// src/pages/Profile.tsx
import {
  Avatar,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CardMedia,
  CircularProgress,
  Container,
  Divider,
  Grid,
  Stack,
  TextField,
  Typography,
  useTheme,
} from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../auth/useAuth";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import { fetchFavoriteRooms } from "../lib/favorites";
import type { FavoriteRoom } from "../types/favorite";
import { extractFavoriteImageUrl } from "../types/favorite";
const HERO_IMAGE =
  "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1600&q=80";

interface ProfileForm {
  nickname: string;
  address: string;
  phoneNumber: string;
  lifeStyle: string;
  hostIntroduction: string;
}

export default function Profile() {
  const theme = useTheme();
  const { user, logout, updateProfile } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [favoriteRooms, setFavoriteRooms] = useState<FavoriteRoom[]>([]);
  const [favoriteLoading, setFavoriteLoading] = useState(false);
  const [favoriteError, setFavoriteError] = useState<string | null>(null);
  const [form, setForm] = useState<ProfileForm>({
    nickname: "",
    address: "",
    phoneNumber: "",
    lifeStyle: "",
    hostIntroduction: "",
  });

  useEffect(() => {
    if (user) {
      setForm({
        nickname: user.nickname ?? "",
        address: user.address ?? "",
        phoneNumber: user.phoneNumber ?? "",
        lifeStyle: user.lifeStyle ?? "",
        hostIntroduction: user.hostIntroduction ?? "",
      });
    }
  }, [user?.id]);

  useEffect(() => {
    let ignore = false;
    if (!user?.id) {
      setFavoriteRooms([]);
      return;
    }
    setFavoriteLoading(true);
    setFavoriteError(null);
    fetchFavoriteRooms(user.id)
      .then((data) => {
        if (!ignore) {
          setFavoriteRooms(data);
        }
      })
      .catch((error) => {
        if (ignore) return;
        const message =
          error instanceof Error
            ? error.message
            : "즐겨찾기 목록을 불러오지 못했습니다.";
        setFavoriteError(message);
        setFavoriteRooms([]);
      })
      .finally(() => {
        if (!ignore) {
          setFavoriteLoading(false);
        }
      });
    return () => {
      ignore = true;
    };
  }, [user?.id]);

  const roles = useMemo(
    () => user?.roles ?? (user?.role ? [user.role] : []),
    [user]
  );

  type EditableTextField =
    | "nickname"
    | "address"
    | "phoneNumber"
    | "lifeStyle"
    | "hostIntroduction";

  const handleChange =
    (field: EditableTextField) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setForm((prev) => ({ ...prev, [field]: event.target.value }));
    };

  const handleCancel = () => {
    if (!user) return;
    setForm({
      nickname: user.nickname ?? "",
      address: user.address ?? "",
      phoneNumber: user.phoneNumber ?? "",
      lifeStyle: user.lifeStyle ?? "",
      hostIntroduction: user.hostIntroduction ?? "",
    });
    setIsEditing(false);
  };

  const handleSave = async () => {
    if (!user) return;
    setIsSaving(true);
    try {
      await updateProfile(form);
      setIsEditing(false);
      alert("프로필이 업데이트되었습니다.");
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "프로필을 저장하는 중 오류가 발생했습니다.";
      alert(message);
    } finally {
      setIsSaving(false);
    }
  };

  if (!user) {
    return (
      <Box minHeight="100vh" display="flex" flexDirection="column">
        <SiteHeader />
        <Box flex={1} display="grid" sx={{ placeItems: "center" }}>
          <Typography>사용자 정보를 불러올 수 없습니다.</Typography>
        </Box>
        <SiteFooter />
      </Box>
    );
  }

  return (
    <Box
      minHeight="100vh"
      display="flex"
      flexDirection="column"
      sx={{ backgroundColor: "#f4f6fb" }}
    >
      <SiteHeader />
      <Container maxWidth="md" sx={{ flex: 1, py: { xs: 6, md: 10 } }}>
        <Box
          sx={{
            bgcolor: "rgba(255,255,255,0.92)",
            borderRadius: 4,
            overflow: "hidden",
            boxShadow: 6,
            position: "relative",
          }}
        >
          <Box
            sx={{
              position: "relative",
              height: { xs: 220, md: 500 },
              backgroundImage: `url(${HERO_IMAGE})`,
              backgroundSize: "cover",
              backgroundPosition: "center",
            }}
          >
            <Box
              sx={{
                position: "absolute",
                inset: 0,
                bgcolor: "rgba(14, 29, 45, 0.55)",
              }}
            />

            <Box
              sx={{
                position: "absolute",
                top: 24,
                right: 24,
                display: "flex",
                gap: 1,
                flexWrap: "wrap",
              }}
            >
              {isEditing ? (
                <>
                  <Button
                    variant="outlined"
                    color="inherit"
                    onClick={handleCancel}
                    disabled={isSaving}
                    sx={{ backgroundColor: "#ffffff" }}
                  >
                    취소
                  </Button>
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleSave}
                    disabled={isSaving}
                  >
                    {isSaving ? "저장 중..." : "저장"}
                  </Button>
                </>
              ) : (
                <>
                  <Button
                    variant="outlined"
                    color="inherit"
                    onClick={() => setIsEditing(true)}
                    sx={{ backgroundColor: "#ffffff" }}
                  >
                    정보 수정
                  </Button>
                  <Button
                    variant="outlined"
                    color="inherit"
                    onClick={logout}
                    sx={{ backgroundColor: "#ffffff" }}
                  >
                    로그아웃
                  </Button>
                </>
              )}
            </Box>

            <Stack
              spacing={1.5}
              alignItems="center"
              position="absolute"
              left="50%"
              top="50%"
              sx={{ transform: "translate(-50%, -30%)" }}
            >
              <Avatar
                sx={{
                  width: { xs: 96, md: 120 },
                  height: { xs: 96, md: 120 },
                  border: "4px solid rgba(255,255,255,0.8)",
                  bgcolor: theme.palette.primary.main,
                  fontSize: 32,
                }}
              >
                {user.nickname?.slice(0, 1)?.toUpperCase() ??
                  user.username.slice(0, 1).toUpperCase()}
              </Avatar>
              <Typography variant="h4" fontWeight={700} color="white">
                {user.nickname || user.username}
              </Typography>
              <Stack
                direction="row"
                spacing={1}
                flexWrap="wrap"
                justifyContent="center"
              >
                {roles.length > 0 ? (
                  roles.map((role) => (
                    <Box
                      key={role}
                      sx={{
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 999,
                        bgcolor: "rgba(255,255,255,0.2)",
                        color: "white",
                        fontSize: 12,
                        letterSpacing: 1,
                      }}
                    >
                      {role}
                    </Box>
                  ))
                ) : (
                  <Box
                    sx={{
                      px: 1.5,
                      py: 0.5,
                      borderRadius: 999,
                      bgcolor: "rgba(255,255,255,0.2)",
                      color: "white",
                      fontSize: 12,
                      letterSpacing: 1,
                    }}
                  >
                    ROLE_PENDING
                  </Box>
                )}
              </Stack>
            </Stack>
          </Box>

          <Box sx={{ p: { xs: 3, md: 5 } }}>
            <Stack spacing={4}>
              <Box>
                <Typography variant="h6" fontWeight={700} gutterBottom>
                  Information
                </Typography>
                <Divider sx={{ mb: 3 }} />
                <Stack spacing={2.5}>
                  <InfoRow label="이메일" value={user.username} />
                  <InfoRow
                    label="닉네임"
                    value={form.nickname}
                    editing={isEditing}
                    onChange={handleChange("nickname")}
                  />
                  <InfoRow
                    label="연락처"
                    value={form.phoneNumber}
                    editing={isEditing}
                    onChange={handleChange("phoneNumber")}
                  />
                  <InfoRow
                    label="주소"
                    value={form.address}
                    editing={isEditing}
                    onChange={handleChange("address")}
                  />
                </Stack>
              </Box>

              <Box>
                <Typography variant="h6" fontWeight={700} gutterBottom>
                  Lifestyle
                </Typography>
                <Divider sx={{ mb: 3 }} />
                <TextAreaRow
                  value={form.lifeStyle}
                  editing={isEditing}
                  onChange={handleChange("lifeStyle")}
                />
              </Box>

              <Box>
                <Typography variant="h6" fontWeight={700} gutterBottom>
                  좋아요한 방
                </Typography>
                <Divider sx={{ mb: 3 }} />
                {favoriteLoading ? (
                  <Stack alignItems="center" py={4}>
                    <CircularProgress />
                  </Stack>
                ) : favoriteError ? (
                  <Typography color="error">{favoriteError}</Typography>
                ) : favoriteRooms.length === 0 ? (
                  <Typography color="text.secondary">
                    아직 좋아요한 방이 없습니다.
                  </Typography>
                ) : (
                  <Grid container spacing={2}>
                    {favoriteRooms.map((item) => (
                      <Grid key={item.roomId} item xs={12} sm={6}>
                        <FavoriteRoomCard room={item} />
                      </Grid>
                    ))}
                  </Grid>
                )}
              </Box>

              {roles.includes("HOST") && (
                <Box>
                  <Typography variant="h6" fontWeight={700} gutterBottom>
                    호스트 정보
                  </Typography>
                  <Divider sx={{ mb: 3 }} />
                  <Stack spacing={2.5}>
                    <Stack spacing={1}>
                      <Typography variant="overline" color="text.secondary">
                        호스트 소개
                      </Typography>
                      {isEditing ? (
                        <TextField
                          multiline
                          minRows={3}
                          value={form.hostIntroduction}
                          onChange={handleChange("hostIntroduction")}
                          fullWidth
                        />
                      ) : (
                        <Typography variant="body1" fontWeight={500}>
                          {form.hostIntroduction || "-"}
                        </Typography>
                      )}
                    </Stack>
                  </Stack>
                </Box>
              )}
            </Stack>
          </Box>
        </Box>
      </Container>
      <SiteFooter />
    </Box>
  );
}

type InfoRowProps = {
  label: string;
  value: string;
  editing?: boolean;
  onChange?: (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => void;
};

function InfoRow({ label, value, editing = false, onChange }: InfoRowProps) {
  return (
    <Stack spacing={1}>
      <Typography variant="overline" color="text.secondary">
        {label}
      </Typography>
      {editing ? (
        <TextField size="small" value={value} onChange={onChange} fullWidth />
      ) : (
        <Typography variant="body1" fontWeight={500}>
          {value || "-"}
        </Typography>
      )}
    </Stack>
  );
}

type TextAreaRowProps = {
  value: string;
  editing?: boolean;
  onChange?: (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => void;
};

function TextAreaRow({ value, editing = false, onChange }: TextAreaRowProps) {
  return editing ? (
    <TextField
      multiline
      minRows={3}
      value={value}
      onChange={onChange}
      fullWidth
    />
  ) : (
    <Typography
      variant="body1"
      fontWeight={500}
      sx={{ whiteSpace: "pre-wrap" }}
    >
      {value || "입력된 라이프스타일 정보가 없습니다."}
    </Typography>
  );
}

type FavoriteRoomCardProps = {
  room: FavoriteRoom;
};

function FavoriteRoomCard({ room }: FavoriteRoomCardProps) {
  const imageUrl = extractFavoriteImageUrl(room.roomImg);
  const likedAtLabel = room.likedAt
    ? new Date(room.likedAt).toLocaleDateString()
    : null;

  return (
    <Card
      sx={{
        height: "100%",
        display: "flex",
        flexDirection: "column",
        borderRadius: 3,
        boxShadow: 3,
      }}
    >
      {imageUrl && (
        <CardMedia
          component="img"
          height="160"
          image={imageUrl}
          alt={room.roomName}
          sx={{ objectFit: "cover" }}
        />
      )}
      <CardContent sx={{ flex: 1 }}>
        <Stack spacing={0.5}>
          <Typography variant="subtitle1" fontWeight={700}>
            {room.roomName}
          </Typography>
          {likedAtLabel && (
            <Typography variant="caption" color="text.secondary">
              {likedAtLabel}에 저장
            </Typography>
          )}
        </Stack>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Button
          size="small"
          component={RouterLink}
          to={room.roomId ? `/rooms/${room.roomId}` : "/rooms"}
          sx={{ borderRadius: 999 }}
        >
          상세 보기
        </Button>
      </CardActions>
    </Card>
  );
}

