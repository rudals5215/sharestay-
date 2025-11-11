import {
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { Link as RouterLink, useParams } from "react-router-dom";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import { api } from "../lib/api";
import type { RoomApiResponse, RoomSummary } from "../types/room";
import { mapRoomFromApi } from "../types/room";

const formatCurrency = (amount?: number) => {
  if (typeof amount !== "number" || Number.isNaN(amount)) return "-";
  return `${amount.toLocaleString()}원/월`;
};

export default function RoomDetail() {
  const { roomId } = useParams<{ roomId: string }>();
  const [room, setRoom] = useState<RoomSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!roomId) {
      setError("잘못된 접근입니다.");
      setIsLoading(false);
      return;
    }

    const fetchRoom = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const { data } = await api.get<RoomApiResponse>(`/rooms/${roomId}`);
        setRoom(mapRoomFromApi(data));
      } catch (err) {
        const message =
          err instanceof Error
            ? err.message
            : "방 정보를 불러오는 중 오류가 발생했습니다.";
        setError(message);
        setRoom(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRoom();
  }, [roomId]);

  return (
    <Box sx={{ bgcolor: "#f4f6fb", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
      <Container maxWidth="md" sx={{ py: { xs: 6, md: 8 } }}>
        {isLoading ? (
          <Stack alignItems="center" spacing={2} py={12}>
            <CircularProgress />
            <Typography color="text.secondary">
              방 정보를 불러오는 중입니다...
            </Typography>
          </Stack>
        ) : error ? (
          <Paper sx={{ p: { xs: 4, md: 6 }, borderRadius: 4 }}>
            <Stack spacing={2} alignItems="center">
              <Typography variant="h6" color="error" fontWeight={700}>
                데이터를 가져오지 못했어요
              </Typography>
              <Typography color="text.secondary" textAlign="center">
                {error}
              </Typography>
              <Button
                variant="contained"
                component={RouterLink}
                to="/rooms"
                sx={{ borderRadius: 999 }}
              >
                방 목록으로 돌아가기
              </Button>
            </Stack>
          </Paper>
        ) : room ? (
          <Paper
            sx={{
              p: { xs: 4, md: 5 },
              borderRadius: 4,
              boxShadow: "0 32px 64px rgba(15, 40, 105, 0.12)",
            }}
          >
            <Stack spacing={3}>
              <Stack direction="row" spacing={2} alignItems="center">
                <Typography variant="h4" fontWeight={800}>
                  {room.title}
                </Typography>
                <Chip
                  label={
                    room.type === "ONE_ROOM"
                      ? "원룸"
                      : room.type === "TWO_ROOM"
                      ? "투룸"
                      : room.type
                  }
                  color="primary"
                  sx={{ borderRadius: 999 }}
                />
              </Stack>

              <Typography variant="h5" color="primary" fontWeight={700}>
                {formatCurrency(room.rentPrice)}
              </Typography>

              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  주소
                </Typography>
                <Typography>{room.address}</Typography>
              </Stack>

              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  상세 설명
                </Typography>
                <Typography whiteSpace="pre-line">{room.description}</Typography>
              </Stack>

              <Stack direction="row" spacing={2}>
                <Button
                  variant="contained"
                  component={RouterLink}
                  to="/rooms"
                  sx={{ borderRadius: 999 }}
                >
                  목록으로 돌아가기
                </Button>
              </Stack>
            </Stack>
          </Paper>
        ) : null}
      </Container>
      <SiteFooter />
    </Box>
  );
}
