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
  reason: string;
  bannedAt: string;
  endDate?: string | null;
  memo?: string | null;
  isActive: boolean;
}

interface User {
  id: number;
  username: string;
  nickname?: string;
  banned: boolean;
}

export default function AdminBans() {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedEmail, setSelectedEmail] = useState<string>("");
  const [banRecords, setBanRecords] = useState<BanRecord[]>([]);
  const [loading, setLoading] = useState(false);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [endDate, setEndDate] = useState<string>("");
  const [memo, setMemo] = useState("");

  // 사용자 목록 가져오기
  const fetchUsers = async () => {
    try {
      const { data } = await api.get<User[]>("/users");
      setUsers(data);
    } catch (err) {
      alert("사용자 목록을 불러오는 중 오류가 발생했습니다.");
    }
  };

  // 선택한 사용자 ban 기록 가져오기 (기록이 없으면 서버에서 단순 상태 확인)
  const fetchBanRecords = async (email: string) => {
    setLoading(true);
    setBanRecords([]); // 이전 기록을 초기화합니다.
    try {
      // 서버에서 기록 API가 없으면 프론트에서 마지막 등록만 보여주도록 초기화
      const user = users.find((u) => u.username === email);
      setBanRecords([
        {
          reason: reason || "-",
          bannedAt: new Date().toISOString(),
          endDate: endDate || null,
          memo: memo || null,
          isActive: user?.banned ?? false,
        },
      ]);
    } catch {
      alert("정지 기록을 불러오는 중 오류가 발생했습니다.");
      if (!user || !user.banned) {
        // 사용자가 존재하지 않거나 정지 상태가 아니면 아무것도 하지 않습니다.
        return;
      }
      // 실제 서버 API 엔드포인트로 교체해야 합니다.
      const { data } = await api.get<BanRecord[]>(`/users/${email}/bans`);
      setBanRecords(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("정지 기록을 불러오는 중 오류가 발생했습니다.", err);
      // 오류 발생 시 빈 배열로 설정하여 UI 일관성을 유지합니다.
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchUsers();
  }, []);

  useEffect(() => {
    if (selectedEmail) {
      void fetchBanRecords(selectedEmail);
    } else {
      setBanRecords([]); // 선택된 사용자가 없으면 목록을 비웁니다.
    }
  }, [selectedEmail]);
  }, [selectedEmail, users]);

  const handleOpenDialog = () => {
    setReason("");
    setEndDate("");
    setMemo("");
    setDialogOpen(true);
  };

  const handleCloseDialog = () => setDialogOpen(false);

  const handleBanSubmit = async () => {
    if (!selectedEmail) return;
    try {
      await api.post(`/users/${selectedEmail}/ban`, {
        reason,
        expireAt: endDate || null,
        memo,
      });
      const user = users.find((u) => u.username === selectedEmail);
      if (user) user.banned = true;
      setUsers([...users]);
      void fetchBanRecords(selectedEmail);
      handleCloseDialog();
    } catch {
      alert("정지 등록 중 오류가 발생했습니다.");
    }
  };

  const handleUnban = async () => {
    if (!selectedEmail) return;
    try {
      await api.post(`/users/${selectedEmail}/unban`);
      const user = users.find((u) => u.username === selectedEmail);
      if (user) user.banned = false;
      setUsers([...users]);
      void fetchBanRecords(selectedEmail);
    } catch {
      alert("정지 해제 중 오류가 발생했습니다.");
    }
  };

  const formatDateTime = (value?: string | null) => {
    if (!value) return "-";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleDateString();
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
            disabled={!selectedEmail}
            onClick={handleOpenDialog}
          >
            정지 등록
          </Button>
        </Stack>

        <TextField
          select
          label="사용자 선택"
          value={selectedEmail}
          onChange={(e) => setSelectedEmail(e.target.value)}
          sx={{ mb: 2, minWidth: 240 }}
          size="small"
        >
          <MenuItem value="">
            <em>선택하세요</em>
          </MenuItem>
          {users.map((user) => (
            <MenuItem key={user.id} value={user.username}>
              {user.nickname ?? user.username} ({user.username}){" "}
              {user.banned ? "[정지]" : ""}
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
                {banRecords.map((ban, idx) => (
                  <TableRow key={idx}>
                    <TableCell>{ban.reason}</TableCell>
                    <TableCell>{formatDateTime(ban.bannedAt)}</TableCell>
                    <TableCell>{formatDateTime(ban.endDate)}</TableCell>
                    <TableCell>{ban.memo ?? "-"}</TableCell>
                    <TableCell>
                      {ban.isActive ? (
                        <Chip label="활성" color="error" size="small" />
                      ) : (
                        <Chip label="해제" size="small" />
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {ban.isActive && (
                        <Button
                          variant="outlined"
                          color="warning"
                          size="small"
                          onClick={handleUnban}
                        >
                          해제
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
              label="종료일(선택)"
              type="datetime-local"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              fullWidth
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
          <Button onClick={handleBanSubmit} variant="contained" color="error">
            등록
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
