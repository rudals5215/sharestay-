// src/pages/Rooms.tsx
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Divider,
  Grid,
  IconButton,
  MenuItem,
  Paper,
  Select,
  Slider,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import {
  Favorite,
  FavoriteBorder,
  GridView,
  ListAlt,
  LocationOn,
  Search,
} from "@mui/icons-material";
import { useEffect, useMemo, useState } from "react";
import { Link as RouterLink } from "react-router-dom";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import { api } from "../lib/api";
import type { ApiEnvelope } from "../auth/types";
import type { RoomSummary } from "../types/room";

const filterFacilities = [
  "에어컨",
  "냉장고",
  "세탁기",
  "인터넷",
  "주차장",
  "헬스장",
  "반려동물 가능",
  "발코니",
];

const districts = [
  { value: "", label: "전체 지역" },
  { value: "강남구", label: "강남구" },
  { value: "마포구", label: "마포구" },
  { value: "광진구", label: "광진구" },
  { value: "송파구", label: "송파구" },
  { value: "용산구", label: "용산구" },
];

const roomTypes = [
  { value: "", label: "전체 유형" },
  { value: "ONE_ROOM", label: "원룸" },
  { value: "TWO_ROOM", label: "투룸" },
  { value: "OFFICETEL", label: "오피스텔" },
  { value: "APARTMENT", label: "아파트" },
];

const formatCurrency = (amount?: number) => {
  if (typeof amount !== "number" || Number.isNaN(amount)) return "-";
  return `${amount.toLocaleString()}원/월`;
};

const extractTags = (room: RoomSummary): string[] => {
  if (Array.isArray(room.tags) && room.tags.length > 0) return room.tags;
  if (Array.isArray(room.options)) return room.options;
  if (typeof room.options === "string") {
    return room.options
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
  }
  return [];
};

const ensureArray = <T,>(value: T | T[]): T[] =>
  Array.isArray(value) ? value : [value];

const availabilityLabel = (status: RoomSummary["availabilityStatus"]) => {
  if (typeof status === "number") {
    if (status === 0) return "모집중";
    if (status === 1) return "예약중";
    return "마감";
  }
  if (typeof status === "string") {
    switch (status.toUpperCase()) {
      case "AVAILABLE":
        return "모집중";
      case "PENDING":
        return "예약중";
      default:
        return "마감";
    }
  }
  return "모집중";
};

export default function Rooms() {
  const [rooms, setRooms] = useState<RoomSummary[]>([]);
  const [keyword, setKeyword] = useState("");
  const [district, setDistrict] = useState("");
  const [roomType, setRoomType] = useState("");
  const [priceRange, setPriceRange] = useState<number[]>([300000, 1500000]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [favorites, setFavorites] = useState<Set<number>>(new Set());

  const priceLabel = useMemo(
    () =>
      `${priceRange[0].toLocaleString()}원 ~ ${priceRange[1].toLocaleString()}원`,
    [priceRange]
  );

  const fetchRooms = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const { data } = await api.get<ApiEnvelope<RoomSummary[]>>("/rooms", {
        params: {
          keyword: keyword || undefined,
          district: district || undefined,
          type: roomType || undefined,
          minRent: priceRange[0],
          maxRent: priceRange[1],
        },
      });
      const list = Array.isArray(data.result) ? data.result : [];
      setRooms(list);
      const favSet = new Set(
        list.filter((room) => room.isFavorite).map((room) => room.roomId)
      );
      setFavorites(favSet);
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : "매물 정보를 불러오는 중 문제가 발생했습니다.";
      setError(message);
      setRooms([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSearch = async (event?: React.FormEvent) => {
    event?.preventDefault();
    await fetchRooms();
  };

  const toggleFavorite = async (room: RoomSummary) => {
    const isFavorite = favorites.has(room.roomId);
    try {
      if (isFavorite) {
        const targetId = room.favoriteId ?? room.roomId;
        await api.delete(`/favorites/${targetId}`);
        setFavorites((prev) => {
          const next = new Set(prev);
          next.delete(room.roomId);
          return next;
        });
        setRooms((prev) =>
          prev.map((item) =>
            item.roomId === room.roomId
              ? { ...item, isFavorite: false, favoriteId: undefined }
              : item
          )
        );
      } else {
        const { data } = await api.post<
          ApiEnvelope<{ favoriteId: number }>
        >("/favorites", { roomId: room.roomId });
        const favoriteId = data.result?.favoriteId ?? room.roomId;
        setFavorites((prev) => new Set(prev).add(room.roomId));
        setRooms((prev) =>
          prev.map((item) =>
            item.roomId === room.roomId
              ? { ...item, isFavorite: true, favoriteId }
              : item
          )
        );
      }
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : "즐겨찾기 처리 중 오류가 발생했습니다.";
      alert(message);
    }
  };

  const createShareLink = async (roomId: number) => {
    try {
      const { data } = await api.post<
        ApiEnvelope<{ link: string }>
      >(`/rooms/${roomId}/share-links`);
      const link = data.result?.link;
      if (link && navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(link).catch(() => undefined);
      }
      alert(
        link
          ? `공유 링크가 생성되었습니다.\n${link}`
          : "공유 링크가 생성되었습니다."
      );
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : "공유 링크를 생성하는 중 오류가 발생했습니다.";
      alert(message);
    }
  };

  return (
    <Box sx={{ bgcolor: "#f4f6fb", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
      <Container maxWidth="lg" sx={{ py: { xs: 6, md: 8 } }}>
        <Stack spacing={3}>
          <Paper
            sx={{
              p: { xs: 3, md: 4 },
              borderRadius: 4,
              boxShadow: "0 24px 40px rgba(15, 40, 105, 0.12)",
            }}
          >
            <Box component="form" onSubmit={handleSearch}>
              <Stack
                direction={{ xs: "column", md: "row" }}
                spacing={2}
                alignItems={{ xs: "stretch", md: "center" }}
              >
                <TextField
                  fullWidth
                  label="지역명 또는 역명을 입력하세요"
                  placeholder="예: 강남역, 홍대입구"
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                />
                <Select
                  value={district}
                  onChange={(event) =>
                    setDistrict(event.target.value as string)
                  }
                  sx={{ minWidth: 160, borderRadius: 3 }}
                >
                  {districts.map((item) => (
                    <MenuItem key={item.value} value={item.value}>
                      {item.label}
                    </MenuItem>
                  ))}
                </Select>
                <Select
                  value={roomType}
                  onChange={(event) =>
                    setRoomType(event.target.value as string)
                  }
                  sx={{ minWidth: 160, borderRadius: 3 }}
                >
                  {roomTypes.map((item) => (
                    <MenuItem key={item.value} value={item.value}>
                      {item.label}
                    </MenuItem>
                  ))}
                </Select>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={<Search />}
                  sx={{ borderRadius: 3, px: 4, py: 1.5 }}
                  disabled={isLoading}
                >
                  {isLoading ? "검색 중..." : "검색"}
                </Button>
              </Stack>
            </Box>
          </Paper>

          <Grid container spacing={4}>
            <Grid size={{ xs: 12, md: 3 }}>
              <Paper
                sx={{
                  p: 3,
                  borderRadius: 4,
                  boxShadow: "0 18px 32px rgba(15, 40, 105, 0.08)",
                }}
              >
                <Stack spacing={3}>
                  <Typography variant="h6" fontWeight={700}>
                    필터
                  </Typography>
                  <Divider />

                  <Stack spacing={1}>
                    <Typography variant="subtitle2" color="text.secondary">
                      방 종류
                    </Typography>
                    {["전체", "원룸", "투룸", "오피스텔", "아파트"].map(
                      (type) => (
                        <Button
                          key={type}
                          variant={type === "전체" ? "contained" : "text"}
                          sx={{ justifyContent: "flex-start", borderRadius: 2 }}
                        >
                          {type}
                        </Button>
                      )
                    )}
                  </Stack>

                  <Stack spacing={1}>
                    <Typography variant="subtitle2" color="text.secondary">
                      가격 범위
                    </Typography>
                    <Slider
                      value={priceRange}
                      min={200000}
                      max={2000000}
                      step={50000}
                      valueLabelDisplay="auto"
                      onChange={(_, value) =>
                        setPriceRange(ensureArray(value as number[]))
                      }
                      onChangeCommitted={() => fetchRooms()}
                    />
                    <Typography variant="caption" color="text.secondary">
                      {priceLabel}
                    </Typography>
                  </Stack>

                  <Stack spacing={1}>
                    <Typography variant="subtitle2" color="text.secondary">
                      편의시설
                    </Typography>
                    <Stack spacing={1} flexWrap="wrap" direction="row" useFlexGap>
                      {filterFacilities.map((facility) => (
                        <Chip
                          key={facility}
                          label={facility}
                          variant="outlined"
                          sx={{ borderRadius: 2 }}
                        />
                      ))}
                    </Stack>
                  </Stack>
                </Stack>
              </Paper>
            </Grid>
            <Grid size={{ xs: 12, md: 9 }}>
              <Stack spacing={3}>
                <Stack
                  direction={{ xs: "column", sm: "row" }}
                  justifyContent="space-between"
                  alignItems={{ xs: "flex-start", sm: "center" }}
                  spacing={2}
                >
                  <Stack spacing={0.5}>
                    <Typography variant="h6" fontWeight={700}>
                      검색 결과
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {rooms.length}개의 매물을 찾았습니다
                    </Typography>
                  </Stack>
                  <Stack direction="row" spacing={1}>
                    <Select defaultValue="추천순" size="small" sx={{ borderRadius: 999 }}>
                      <MenuItem value="추천순">추천순</MenuItem>
                      <MenuItem value="가격낮은순">가격 낮은 순</MenuItem>
                      <MenuItem value="가격높은순">가격 높은 순</MenuItem>
                    </Select>
                    <IconButton>
                      <GridView />
                    </IconButton>
                    <IconButton>
                      <ListAlt />
                    </IconButton>
                  </Stack>
                </Stack>

                {error && (
                  <Paper sx={{ p: 3, borderRadius: 3, bgcolor: "#fff5f5" }}>
                    <Typography color="error">{error}</Typography>
                  </Paper>
                )}

                {isLoading ? (
                  <Box
                    sx={{
                      display: "grid",
                      placeItems: "center",
                      minHeight: 240,
                    }}
                  >
                    <CircularProgress />
                  </Box>
                ) : rooms.length === 0 ? (
                  <Paper sx={{ p: 4, borderRadius: 3 }}>
                    <Typography color="text.secondary" textAlign="center">
                      표시할 매물이 없습니다. 검색 조건을 변경해보세요.
                    </Typography>
                  </Paper>
                ) : (
                  <Grid container spacing={3}>
                    {rooms.map((room) => {
                      const isFavorite = favorites.has(room.roomId);
                      const tags = extractTags(room);
                      const imageUrl =
                        room.images?.[0]?.imageUrl ??
                        "https://images.unsplash.com/photo-1505691723518-36a5ac3be353?auto=format&fit=crop&w=900&q=80";
                      return (
                        <Grid size={{ xs: 12, sm: 6 }} key={room.roomId}>
                          <Card
                            sx={{
                              borderRadius: 4,
                              overflow: "hidden",
                              boxShadow: "0 20px 40px rgba(15, 40, 105, 0.12)",
                              height: "100%",
                              display: "flex",
                              flexDirection: "column",
                            }}
                          >
                            <Box
                              component="img"
                              src={imageUrl}
                              alt={room.title}
                              sx={{ height: 200, width: "100%", objectFit: "cover" }}
                            />
                            <CardContent sx={{ display: "grid", gap: 1.5, flexGrow: 1 }}>
                              <Stack direction="row" spacing={1}>
                                {typeof room.safetyScore === "number" && (
                                  <Chip
                                    label={`안전도 ${Math.round(room.safetyScore)}`}
                                    color="primary"
                                    size="small"
                                    sx={{ borderRadius: 999 }}
                                  />
                                )}
                                {typeof room.trustScore === "number" && (
                                  <Chip
                                    label={`신뢰도 ${Math.round(room.trustScore)}`}
                                    color="success"
                                    size="small"
                                    sx={{ borderRadius: 999 }}
                                  />
                                )}
                                <Chip
                                  label={availabilityLabel(room.availabilityStatus)}
                                  size="small"
                                  sx={{ borderRadius: 999 }}
                                />
                              </Stack>
                              <Typography variant="h6" fontWeight={700}>
                                {room.title}
                              </Typography>
                              <Stack direction="row" spacing={1} alignItems="center">
                                <LocationOn fontSize="small" color="action" />
                                <Typography variant="body2" color="text.secondary">
                                  {room.address}
                                </Typography>
                              </Stack>
                              <Typography variant="body1" fontWeight={700} color="primary">
                                {formatCurrency(room.rentPrice)}
                              </Typography>
                              {room.description && (
                                <Typography variant="body2" color="text.secondary">
                                  {room.description}
                                </Typography>
                              )}
                              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                                {tags.slice(0, 4).map((tag) => (
                                  <Chip
                                    key={tag}
                                    label={tag}
                                    size="small"
                                    sx={{ borderRadius: 999 }}
                                  />
                                ))}
                              </Stack>
                            </CardContent>
                            <CardActions
                              sx={{
                                px: 3,
                                pb: 3,
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                gap: 1,
                              }}
                            >
                              <Button
                                variant="text"
                                component={RouterLink}
                                to={`/rooms?highlight=${room.roomId}`}
                              >
                                자세히 보기
                              </Button>
                              <Button
                                variant="outlined"
                                size="small"
                                sx={{ borderRadius: 999 }}
                                onClick={() => createShareLink(room.roomId)}
                              >
                                공유 링크
                              </Button>
                              <IconButton
                                onClick={() => toggleFavorite(room)}
                                color={isFavorite ? "error" : "default"}
                                aria-label="즐겨찾기"
                              >
                                {isFavorite ? <Favorite /> : <FavoriteBorder />}
                              </IconButton>
                            </CardActions>
                          </Card>
                        </Grid>
                      );
                    })}
                  </Grid>
                )}

                <Stack direction="row" justifyContent="center" spacing={1}>
                  {[1, 2, 3].map((page) => (
                    <Button
                      key={page}
                      variant={page === 1 ? "contained" : "outlined"}
                      sx={{ borderRadius: 999, minWidth: 44 }}
                    >
                      {page}
                    </Button>
                  ))}
                </Stack>
              </Stack>
            </Grid>
          </Grid>
        </Stack>
      </Container>
      <SiteFooter />
    </Box>
  );
}
