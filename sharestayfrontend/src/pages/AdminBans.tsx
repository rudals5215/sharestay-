import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
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
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import CancelScheduleSendOutlinedIcon from "@mui/icons-material/CancelScheduleSendOutlined";
import { useEffect, useMemo, useState } from "react";
import { api } from "../lib/api";

interface BanRecord {
  banId: number;
  userId: number;
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
}

type DurationPreset = "1d" | "7d" | "30d" | "permanent";

export default function AdminBans() {
  const [users, setUsers] = useState<User[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [banRecords, setBanRecords] = useState<BanRecord[]>([]);
  const [loading, setLoading] = useState(false);

  const [registerDialogOpen, setRegisterDialogOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [endDate, setEndDate] = useState<string>("");
  const [memo, setMemo] = useState("");

  const [unbanDialogOpen, setUnbanDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [targetBan, setTargetBan] = useState<BanRecord | null>(null);
  const [editReason, setEditReason] = useState("");
  const [editEndDate, setEditEndDate] = useState("");
  const [editMemo, setEditMemo] = useState("");

  const userLabelMap = useMemo(
    () =>
      users.reduce<Record<number, string>>((map, user) => {
        map[user.id] = `${user.nickname ?? user.username} (${user.username})`;
        return map;
      }, {}),
    [users]
  );

  const formatDateTime = (value?: string | null) => {
    if (!value) return "-";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString();
  };

  const fetchUsers = async () => {
    const { data } = await api.get<User[]>("/users");
    setUsers(data);
  };

  const fetchBanRecords = async (userId: number) => {
    setLoading(true);
    try {
      const { data } = await api.get<BanRecord[]>(`/bans/users/${userId}`);
      setBanRecords(data);
    } catch (err) {
      alert("정지 기록을 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const fetchAllBans = async () => {
    setLoading(true);
    try {
      const { data } = await api.get<BanRecord[]>(`/bans`);
      setBanRecords(data);
    } catch (err) {
      alert("전체 정지 기록을 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchUsers();
    void fetchAllBans();
  }, []);

  useEffect(() => {
    if (!selectedUserId) {
      void fetchAllBans();
      return;
    }
    void fetchBanRecords(selectedUserId);
  }, [selectedUserId]);

  const handleOpenRegisterDialog = () => {
    setReason("");
    setEndDate("");
    setMemo("");
    setRegisterDialogOpen(true);
  };

  const handleBanSubmit = async () => {
    if (!selectedUserId) {
      alert("먼저 사용자를 선택해주세요.");
      return;
    }
    if (!reason.trim()) {
      alert("정지 사유를 입력해주세요.");
      return;
    }
    try {
      await api.post(`/bans/users/${selectedUserId}`, {
        reason,
        expireAt: endDate || null,
        memo,
      });
      if (selectedUserId) {
        void fetchBanRecords(selectedUserId);
      }
      setRegisterDialogOpen(false);
    } catch (err) {
      alert("정지 등록 중 오류가 발생했습니다.");
    }
  };

  const handleDurationPreset = (preset: DurationPreset, setter: (value: string) => void) => {
    const now = new Date();
    let date: Date | null = null;
    switch (preset) {
      case "1d":
        date = new Date(now.getTime() + 24 * 60 * 60 * 1000);
        break;
      case "7d":
        date = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
        break;
      case "30d":
        date = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
        break;
      case "permanent":
        date = null;
        break;
    }
    setter(
      date
        ? new Date(date.getTime() - date.getTimezoneOffset() * 60000)
            .toISOString()
            .slice(0, 16)
        : ""
    );
  };

  const openUnbanDialog = (ban: BanRecord) => {
    setTargetBan(ban);
    setUnbanDialogOpen(true);
  };

  const openEditDialog = (ban: BanRecord) => {
    setTargetBan(ban);
    setEditReason(ban.reason);
    setEditEndDate(
      ban.endDate
        ? new Date(new Date(ban.endDate).getTime() - new Date().getTimezoneOffset() * 60000)
            .toISOString()
            .slice(0, 16)
        : ""
    );
    setEditMemo(ban.memo ?? "");
    setEditDialogOpen(true);
  };

  const handleUnbanConfirm = async () => {
    if (!targetBan) return;
    try {
      await api.delete(`/bans/${targetBan.banId}`);
      if (selectedUserId) {
        void fetchBanRecords(selectedUserId);
      } else {
        void fetchAllBans();
      }
      setUnbanDialogOpen(false);
      setTargetBan(null);
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
              사용자를 선택하여 정지 기록을 조회·등록·해제할 수 있습니다. 미선택 시 전체 기록을 보여줍니다.
            </Typography>
          </Box>
          <Button
            variant="contained"
            disabled={!selectedUserId}
            onClick={handleOpenRegisterDialog}
            startIcon={<CancelScheduleSendOutlinedIcon />}
          >
            정지 등록
          </Button>
        </Stack>

        <TextField
          select
          label="사용자 선택"
          value={selectedUserId ?? ""}
          onChange={(e) => {
            const value = e.target.value;
            setSelectedUserId(value === "" ? null : Number(value));
          }}
          sx={{ mb: 2, minWidth: 260 }}
          size="small"
        >
          <MenuItem value="">
            <em>전체 보기</em>
          </MenuItem>
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
                  <TableCell>사용자</TableCell>
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
                  <TableRow key={ban.banId}>
                    <TableCell>{userLabelMap[ban.userId] ?? `#${ban.userId}`}</TableCell>
                    <TableCell>{ban.reason}</TableCell>
                    <TableCell>{formatDateTime(ban.bannedAt)}</TableCell>
                    <TableCell>{formatDateTime(ban.endDate)}</TableCell>
                    <TableCell>{ban.memo ?? "-"}</TableCell>
                    <TableCell>
                      {ban.isActive ? (
                        <Chip label="정지" color="error" size="small" />
                      ) : (
                        <Chip label="해제" size="small" />
                      )}
                    </TableCell>
                    <TableCell align="right">
                      {ban.isActive && (
                        <>
                          <IconButton
                            aria-label="정지 수정"
                            onClick={() => openEditDialog(ban)}
                            size="small"
                          >
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                          <IconButton
                            aria-label="정지 해제"
                            onClick={() => openUnbanDialog(ban)}
                            size="small"
                          >
                            <CancelScheduleSendOutlinedIcon fontSize="small" />
                          </IconButton>
                        </>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </Box>
      </Paper>

      <Dialog open={registerDialogOpen} onClose={() => setRegisterDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>사용자 정지 등록</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField
              label="정지 사유"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="종료일(선택)"
              type="datetime-local"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              fullWidth
            />
            <Stack direction="row" spacing={1} flexWrap="wrap">
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("1d", setEndDate)}>
                +1일
              </Button>
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("7d", setEndDate)}>
                +7일
              </Button>
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("30d", setEndDate)}>
                +30일
              </Button>
              <Button variant="text" size="small" onClick={() => handleDurationPreset("permanent", setEndDate)}>
                영구
              </Button>
            </Stack>
            <TextField
              label="메모 (선택)"
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              multiline
              minRows={3}
              fullWidth
            />
            <Typography variant="caption" color="text.secondary">
              종료일을 비워두면 영구 정지로 처리됩니다.
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRegisterDialogOpen(false)}>취소</Button>
          <Button onClick={handleBanSubmit} variant="contained">
            등록
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={unbanDialogOpen} onClose={() => setUnbanDialogOpen(false)}>
        <DialogTitle>정지 해제</DialogTitle>
        <DialogContent>
          <Stack spacing={1.5} mt={1}>
            <Typography>
              {targetBan
                ? `${userLabelMap[targetBan.userId] ?? `#${targetBan.userId}`} 님의 정지를 해제하시겠습니까?`
                : "정지를 해제하시겠습니까?"}
            </Typography>
            {targetBan && (
              <>
                <Typography variant="body2" color="text.secondary">
                  사유: {targetBan.reason}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  종료일: {formatDateTime(targetBan.endDate)}
                </Typography>
              </>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUnbanDialogOpen(false)}>취소</Button>
          <Button onClick={handleUnbanConfirm} variant="contained" color="error">
            해제
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>정지 내용 수정</DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField
              label="정지 사유"
              value={editReason}
              onChange={(e) => setEditReason(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="종료일(선택)"
              type="datetime-local"
              value={editEndDate}
              onChange={(e) => setEditEndDate(e.target.value)}
              fullWidth
            />
            <Stack direction="row" spacing={1} flexWrap="wrap">
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("1d", setEditEndDate)}>
                +1일
              </Button>
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("7d", setEditEndDate)}>
                +7일
              </Button>
              <Button variant="outlined" size="small" onClick={() => handleDurationPreset("30d", setEditEndDate)}>
                +30일
              </Button>
              <Button variant="text" size="small" onClick={() => handleDurationPreset("permanent", setEditEndDate)}>
                영구
              </Button>
            </Stack>
            <TextField
              label="메모 (선택)"
              value={editMemo}
              onChange={(e) => setEditMemo(e.target.value)}
              multiline
              minRows={3}
              fullWidth
            />
            <Typography variant="caption" color="text.secondary">
              종료일을 비워두면 영구 정지로 처리됩니다.
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>취소</Button>
          <Button
            onClick={async () => {
              if (!targetBan) return;
              try {
                await api.patch(`/bans/${targetBan.banId}`, {
                  reason: editReason,
                  expireAt: editEndDate || null,
                  memo: editMemo,
                });
                if (selectedUserId) {
                  void fetchBanRecords(selectedUserId);
                } else {
                  void fetchAllBans();
                }
                setEditDialogOpen(false);
              } catch (err) {
                alert("정지 수정 중 오류가 발생했습니다.");
              }
            }}
            variant="contained"
          >
            저장
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
