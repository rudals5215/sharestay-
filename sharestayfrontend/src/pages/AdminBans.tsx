import {
  Box,
  Button,
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

  useEffect(() => {
    fetchUsers();
  }, []);

  useEffect(() => {
    if (selectedUserId) {
      fetchBanRecords(selectedUserId);
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
        expireAt: endDate || null,
        memo,
      });
      fetchBanRecords(selectedUserId);
      handleCloseDialog();
    } catch (err) {
      alert("정지 등록 중 오류가 발생했습니다.");
    }
  };

  const handleUnban = async (banId: number) => {
    if (!selectedUserId) return;
    try {
      await api.delete(`/bans/${banId}`);
      fetchBanRecords(selectedUserId);
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
            value={selectedUserId?.toString() ?? ""}
            onChange={(e) => setSelectedUserId(Number(e.target.value))}
            SelectProps={{ native: true }}
            sx={{ mb: 2, minWidth: 240 }}
            size="small"
          >
          <option value="">선택하세요</option>
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.username}
            </option>
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
                    <TableCell>
                      {new Date(ban.bannedAt).toLocaleString()}
                    </TableCell>
                    <TableCell>
                      {ban.endDate
                        ? new Date(ban.endDate).toLocaleString()
                        : "-"}
                    </TableCell>
                    <TableCell>{ban.memo ?? "-"}</TableCell>
                    <TableCell>{ban.isActive ? "활성" : "해제"}</TableCell>
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
