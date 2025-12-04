// src/pages/Contact.tsx
import {
  Box,
  Button,
  Container,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import { Link as RouterLink } from "react-router-dom";

export default function Contact() {
  return (
    <Box sx={{ bgcolor: "#f4f6fb", minHeight: "100vh" }}>
      <SiteHeader />
      <Container maxWidth="sm" sx={{ py: { xs: 6, md: 8 } }}>
        <Paper
          sx={{
            p: { xs: 3, md: 4 },
            borderRadius: 4,
            boxShadow: "0 24px 48px rgba(15, 40, 105, 0.08)",
          }}
        >
          <Stack spacing={3} textAlign="center">
            <Stack spacing={1}>
              <Typography variant="h5" fontWeight={800}>
                문의하기
              </Typography>
              <Typography color="text.secondary">
                서비스 이용 중 궁금한 점이나 불편한 점이 있으시면 언제든지
                문의해주세요.
              </Typography>
            </Stack>
            <TextField label="이름" placeholder="이름을 입력하세요" fullWidth />
            <TextField
              label="이메일 주소"
              placeholder="example@sharestay.kr"
              fullWidth
            />
            <TextField label="문의 내용" multiline rows={4} fullWidth />
            <Button variant="contained" size="large">
              문의 보내기
            </Button>
            <Button variant="text" component={RouterLink} to="/">
              홈으로 돌아가기
            </Button>
          </Stack>
        </Paper>
      </Container>
      <SiteFooter />
    </Box>
  );
}