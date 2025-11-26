import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
  CircularProgress,
  MenuItem,
} from "@mui/material";
import { useEffect, useState } from "react";
import { api } from "../lib/api";

interface BanRecord {
  id: number;
  reason: string;
  bannedAt: string;
  endDate?: string;
  memo?: string;
  isActive: boolean;
}

interface User {
  id: number;
  username: string;
  nickname?: string;
}

export default function AdminBans() {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [banRecords, setBanRecords] = useState<BanRecord[]>([]);
  const [loading, setLoading] = useState(false);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [endDate, setEndDate] = useState<string | undefined>(undefined);
  const [memo, setMemo] = useState("");

  const fetchUsers = async () => {
    const { data } = await api.get<User[]>("/users");
    setUsers(data);
  };

  const fetchBanRecords = async (userId: number) => {
    setLoading(true);
    const { data } = await api.get<BanRecord[]>(`/bans/users/${userId}`);
    setBanRecords(data);
    setLoading(false);
  };

  // 날짜 및 시간 형식을 안전하게 변환하는 헬퍼 함수
  const formatDateTime = (dateString?: string | null) => {
    if (!dateString) return "-";
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) { // 유효하지 않은 날짜인 경우
        return "-";
      }
      return date.toLocaleString();
    } catch (e) {
      return "-"; // 변환 중 오류 발생 시
    }
  };

  useEffect(() => {
    void fetchUsers();
  }, []);

  useEffect(() => {
    if (selectedUserId) {
      void fetchBanRecords(selectedUserId);
    }
  }, [selectedUserId]);

  const handleOpenDialog = () => {
    setReason("");
    setEndDate("");
    setMemo("");
    setDialogOpen(true);
  };

  const handleCloseDialog = () => setDialogOpen(false);

  const handleBanSubmit = async () => {
    if (!selectedUserId) return;

    try {
      await api.post(`/bans/users/${selectedUserId}`, {
        reason,
        // endDate가 존재하면 ISO 8601 형식의 UTC 시간으로 변환하여 전송
        // "2024-01-01T10:00" -> "2024-01-01T10:00:00.000Z"
        expireAt: endDate ? new Date(endDate).toISOString() : null,
        memo,
      });
      void fetchBanRecords(selectedUserId);
      handleCloseDialog();
    } catch (err) {
      alert("정지 등록 중 오류가 발생했습니다.");
    }
  };

  const handleUnban = async (banId: number) => {
    if (!selectedUserId) return;
    try {
      await api.delete(`/bans/${banId}`);
      void fetchBanRecords(selectedUserId);
    } catch (err) {
      alert("정지 해제 중 오류가 발생했습니다.");
    }
  };

  return (
    <>
      <Paper sx={{ p: { xs: 2, md: 3 }, borderRadius: 3, boxShadow: 4 }}>
        <Stack
          direction="row"
          justifyContent="space-between"
          alignItems="center"
          mb={3}
        >
          <Box>
            <Typography variant="h5" fontWeight={700}>
              사용자 정지 관리
            </Typography>
            <Typography variant="body2" color="text.secondary">
              사용자를 선택하여 정지 기록을 보거나 정지시킬 수 있습니다.
            </Typography>
          </Box>
          <Button
            variant="contained"
            disabled={!selectedUserId}
            onClick={handleOpenDialog}
          >
            정지 등록
          </Button>
        </Stack>

        <TextField
            select
            label="사용자 선택"
            value={selectedUserId ?? ""}
            onChange={(e) => setSelectedUserId(Number(e.target.value))}
            sx={{ mb: 2, minWidth: 240 }}
            size="small"
          >
          <MenuItem value=""><em>선택하세요</em></MenuItem>
          {users.map((user) => (
            <MenuItem key={user.id} value={user.id}>
              {user.nickname ?? user.username} ({user.username})
            </MenuItem>
          ))}
        </TextField>

        <Box sx={{ overflowX: "auto" }}>
          {loading ? (
            <Box display="grid" sx={{ placeItems: "center" }} minHeight={200}>
              <CircularProgress />
            </Box>
          ) : (
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>사유</TableCell>
                  <TableCell>시작일</TableCell>
                  <TableCell>종료일</TableCell>
                  <TableCell>메모</TableCell>
                  <TableCell>상태</TableCell>
                  <TableCell align="right">관리</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {banRecords.map((ban) => (
                  <TableRow key={ban.id}>
                    <TableCell>{ban.reason}</TableCell>
                    <TableCell>{formatDateTime(ban.bannedAt)}</TableCell>
                    <TableCell>{formatDateTime(ban.endDate)}</TableCell>
                    <TableCell>{ban.memo ?? "-"}</TableCell>
                    <TableCell>
                      {ban.isActive ? (
                        <Chip label="활성" color="error" size="small" />
                      ) : (<Chip label="해제" size="small" />)}
                    </TableCell>
                    <TableCell align="right">
                      {ban.isActive && (
                        <Button
                          variant="outlined"
                          color="error"
                          size="small"
                          onClick={() => handleUnban(ban.id)}
                        >
                          정지 해제
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </Box>
      </Paper>

      {/* 정지 등록 다이얼로그 */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog}>
        <DialogTitle>사용자 정지 등록</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField
              label="정지 사유"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              fullWidth
            />
            <TextField
              label="종료일 (선택)"
              type="datetime-local"
              value={endDate ?? ""}
              onChange={(e) => setEndDate(e.target.value)}
              fullWidth
              // label이 날짜 형식과 겹치지 않도록 항상 작게 표시
              InputLabelProps={{
                shrink: true,
              }}
            />
            <TextField
              label="메모 (선택)"
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              multiline
              minRows={3}
              fullWidth
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>취소</Button>
          <Button onClick={handleBanSubmit} variant="contained">
            등록
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
