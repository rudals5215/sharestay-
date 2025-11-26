// src/pages/ListRoom.tsx  방등록
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Container,
  FormControlLabel,
  Grid,
  InputAdornment,
  MenuItem,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { HomeWork, LocationOn } from "@mui/icons-material";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useState, useRef } from "react";
import type { Dispatch, SetStateAction } from "react";
import { useNavigate } from "react-router-dom";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import FormTextField from "../components/FormTextField";
import { api } from "../lib/api";
import { useAuth } from "../auth/useAuth";
import type {
  RoomAvailabilityStatus,
  RoomApiResponse,
  RoomRequestPayload,
} from "../types/room";

// ------------------------------
// Zod Schema
// ------------------------------
const roomSchema = z.object({
  title: z.string().min(1, "모집 제목을 입력해주세요."),
  rentPrice: z
    .string()
    .min(1, "월세를 입력해주세요.")
    .refine(
      (value) => !Number.isNaN(Number(value)),
      "월세는 숫자로 입력해주세요."
    ),
  type: z.string().min(1, "방 유형을 선택해주세요."),
  availabilityStatus: z.string().min(1, "모집 상태를 선택해주세요."),
  address: z.string().min(1, "주소를 입력해주세요."),
  latitude: z
    .string()
    .optional()
    .refine(
      (value) => !value || !Number.isNaN(Number(value)),
      "위도는 숫자로 입력해주세요."
    ),
  longitude: z
    .string()
    .optional()
    .refine(
      (value) => !value || !Number.isNaN(Number(value)),
      "경도는 숫자로 입력해주세요."
    ),
  description: z
    .string()
    .min(10, "상세 설명을 10자 이상 작성해주세요.")
    .max(1000, "상세 설명은 1000자 이하로 작성해주세요."),
});

type FormValues = z.infer<typeof roomSchema>;

// ------------------------------
// Options
// ------------------------------
const roomTypes = [
  { value: "ONE_ROOM", label: "원룸" },
  { value: "TWO_ROOM", label: "투룸" },
  { value: "OFFICETEL", label: "오피스텔" },
  { value: "APARTMENT", label: "아파트" },
  { value: "ETC", label: "기타" },
];

const availabilityOptions = [
  { value: "AVAILABLE", label: "모집중" },
  { value: "PENDING", label: "예약중" },
  { value: "UNAVAILABLE", label: "마감" },
];

const availabilityStatusMap: Record<RoomAvailabilityStatus, number> = {
  AVAILABLE: 1,
  PENDING: 0,
  UNAVAILABLE: -1,
};

const lifestyleOptions = [
  "금연",
  "흡연",
  "조용한 생활",
  "사교적",
  "청소 자주",
  "요리 자주",
  "늦게 귀가",
  "일찍 기상",
  "운동 좋아함",
  "음악 감상",
  "게임",
  "독서",
];

const facilityOptions = [
  "주차",
  "와이파이",
  "세탁기",
  "에어컨",
  "냉장고",
  "전자레인지",
  "가스레인지",
  "TV",
  "침대",
  "책상",
  "옷장",
  "베란다",
  "엘리베이터",
  "보안시설",
  "반려동물 가능",
];

export default function ListRoom() {
  const {
    control,
    handleSubmit,
    reset,
    formState: { isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(roomSchema),
    defaultValues: {
      title: "",
      rentPrice: "",
      type: "",
      availabilityStatus: "AVAILABLE",
      address: "",
      latitude: "",
      longitude: "",
      description: "",
    },
  });

  const navigate = useNavigate();
  const { user } = useAuth();
  const hostId = user?.id ?? null;
  const roleList = user?.roles ?? (user?.role ? [user.role] : []);
  const isHostUser =
    roleList.includes("HOST") || roleList.includes("ADMIN");
  const canSubmit = Boolean(hostId && isHostUser);

  const [selectedLifestyle, setSelectedLifestyle] = useState<string[]>([]);
  const [selectedFacilities, setSelectedFacilities] = useState<string[]>([]);
  const [images, setImages] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const toggleSelection = (
    value: string,
    setter: Dispatch<SetStateAction<string[]>>
  ) =>
    setter((prev: string[]) =>
      prev.includes(value)
        ? prev.filter((item) => item !== value)
        : [...prev, value]
    );

  const handleImagePick = () => {
    fileInputRef.current?.click();
  };

  const handleImagesChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []).slice(0, 6);
    setImages(files);
  };

  const handleReset = () => {
    reset();
    setSelectedLifestyle([]);
    setSelectedFacilities([]);
    setImages([]);
  };

  const onSubmit = async (values: FormValues) => {
    if (!canSubmit) {
      alert("이 기능은 호스트 전용입니다. 호스트 전환을 완료해 주세요.");
      return;
    }
    try {
      const rentPrice = Number(values.rentPrice);
      const latitudeValue =
        values.latitude && values.latitude.trim().length > 0
          ? Number(values.latitude)
          : undefined;
      const longitudeValue =
        values.longitude && values.longitude.trim().length > 0
          ? Number(values.longitude)
          : undefined;

      if (Number.isNaN(rentPrice)) {
        throw new Error("월세 값이 올바르지 않습니다.");
      }
      if (latitudeValue !== undefined && Number.isNaN(latitudeValue)) {
        throw new Error("위도 값이 올바르지 않습니다.");
      }
      if (longitudeValue !== undefined && Number.isNaN(longitudeValue)) {
        throw new Error("경도 값이 올바르지 않습니다.");
      }

      const availabilityCode =
        availabilityStatusMap[
          values.availabilityStatus as RoomAvailabilityStatus
        ] ?? 0;

      const selectedOptions = Array.from(
        new Set([...selectedLifestyle, ...selectedFacilities])
      );
      const optionsBlock = selectedOptions.length
        ? `선호 옵션:\n${selectedOptions.map((option) => `- ${option}`).join("\n")}`
        : "";
      const composedDescription = [values.description, optionsBlock]
        .filter(Boolean)
        .join("\n\n");

        // ⭐ 변경 1: JSON payload 대신 FormData 생성
    const formData = new FormData();
    formData.append("hostId", String(hostId!));
    formData.append("title", values.title);
    formData.append("rentPrice", String(rentPrice));
    formData.append("address", values.address);
    formData.append("type", values.type);
    formData.append("availabilityStatus", String(availabilityCode));
    formData.append("description", composedDescription);
    formData.append("latitude", String(latitudeValue ?? 0));
    formData.append("longitude", String(longitudeValue ?? 0));

      // ⭐ 변경 2: 이미지 파일들을 files 필드로 함께 전송
    images.forEach((file) => {
      formData.append("files", file);
    });

    // ⭐ 변경 3: 엔드포인트 + multipart 전송으로 한 번에 요청
    //   - api 인스턴스 baseURL이 `http://localhost:8080` 이라면 "/api/rooms"
    //   - baseURL이 `http://localhost:8080/api` 라면 "/rooms"로 맞추기
    const { data } = await api.post<RoomApiResponse>(
      "/rooms", // 🔥 여기 엔드포인트가 핵심
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      }
    );

    const createdRoomId = data?.id ?? (data as { roomId?: number }).roomId;

    // ⭐ 변경 4: 별도 /images 업로드 API는 더 이상 호출 X
    // if (createdRoomId && images.length > 0) {
    //   const formData = new FormData();
    //   images.forEach((file) => formData.append("files", file));
    //   await api.post(`/rooms/${createdRoomId}/images`, formData, {
    //     headers: { "Content-Type": "multipart/form-data" },
    //   });
    // }

      alert("룸 정보가 등록되었습니다.");
      handleReset();
      navigate(
        createdRoomId ? `/rooms?highlight=${createdRoomId}` : "/rooms",
        { replace: true }
      );
    } catch (err) {
      const message =
        err instanceof Error
          ? err.message
          : "룸 정보를 등록하는 중 오류가 발생했습니다.";
      alert(message);
    }
  };

  return (
    <Box sx={{ bgcolor: "#f4f6fb", minHeight: "100vh" }}>
      <SiteHeader activePath="/list-room" />
      <Container maxWidth="lg" sx={{ py: { xs: 6, md: 8 } }}>
        <Stack spacing={4}>
          <Stack spacing={1}>
            <Typography variant="h4" fontWeight={800}>
              룸메이트 모집하기
            </Typography>
            <Typography color="text.secondary">
              새로운 룸메이트를 모집해보세요. 정확한 정보 입력이 중요합니다.
            </Typography>
          </Stack>

          {!canSubmit && (
            <Alert severity="warning" sx={{ borderRadius: 3 }}>
              호스트 전환을 완료해야 방을 등록할 수 있습니다.
            </Alert>
          )}

          <Paper
            sx={{
              p: { xs: 3, md: 4 },
              borderRadius: 4,
              boxShadow: "0 24px 48px rgba(15, 40, 105, 0.08)",
            }}
            component="form"
            onSubmit={handleSubmit(onSubmit)}
          >
            <Stack spacing={4}>
              <SectionTitle
                icon={<HomeWork color="primary" />}
                title="기본 정보"
              />

              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <FormTextField
                    name="title"
                    control={control}
                    label="모집 제목"
                    placeholder="예: 강남역 도보 5분 깔끔한 원룸 룸메이트 구해요"
                  />
                </Grid>

                <Grid item xs={12} md={4}>
                  <FormTextField
                    name="rentPrice"
                    control={control}
                    label="월세 (원)"
                    placeholder="예: 425000"
                    inputMode="numeric"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₩</InputAdornment>
                      ),
                    }}
                  />
                </Grid>

                <Grid item xs={12} md={4}>
                  <FormTextField
                    name="type"
                    control={control}
                    label="방 유형"
                    select
                    defaultValue=""
                  >
                    <MenuItem value="">
                      <em>방 유형을 선택하세요</em>
                    </MenuItem>
                    {roomTypes.map((type) => (
                      <MenuItem key={type.value} value={type.value}>
                        {type.label}
                      </MenuItem>
                    ))}
                  </FormTextField>
                </Grid>

                <Grid item xs={12} md={4}>
                  <FormTextField
                    name="availabilityStatus"
                    control={control}
                    label="모집 상태"
                    select
                  >
                    {availabilityOptions.map((option) => (
                      <MenuItem key={option.value} value={option.value}>
                        {option.label}
                      </MenuItem>
                    ))}
                  </FormTextField>
                </Grid>
              </Grid>

              <SectionTitle
                icon={<LocationOn color="primary" />}
                title="주소 및 위치"
              />

              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <FormTextField
                    name="address"
                    control={control}
                    label="주소"
                    placeholder="예: 서울특별시 강남구 역삼동 123-45"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormTextField
                    name="latitude"
                    control={control}
                    label="위도 (선택)"
                    placeholder="예: 37.4981"
                  />
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormTextField
                    name="longitude"
                    control={control}
                    label="경도 (선택)"
                    placeholder="예: 127.0276"
                  />
                </Grid>
              </Grid>

              <SectionTitle title="생활 패턴" />
              <CheckboxGroup
                options={lifestyleOptions}
                selected={selectedLifestyle}
                onToggle={(option) =>
                  toggleSelection(option, setSelectedLifestyle)
                }
              />

              <SectionTitle title="부가 옵션" />
              <CheckboxGroup
                options={facilityOptions}
                selected={selectedFacilities}
                onToggle={(option) =>
                  toggleSelection(option, setSelectedFacilities)
                }
              />

              <SectionTitle title="상세 설명" />
              <FormTextField
                name="description"
                control={control}
                label="상세 설명"
                placeholder="룸메이트에 대한 상세한 설명을 작성해주세요. (최소 10자 이상)"
                multiline
                minRows={6}
              />

              <SectionTitle title="사진 업로드" />
              <Stack spacing={2}>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  style={{ display: "none" }}
                  ref={fileInputRef}
                  onChange={handleImagesChange}
                />
                <Button variant="outlined" onClick={handleImagePick}>
                  사진 추가 ({images.length}/6)
                </Button>

                {images.length > 0 && (
                  <Stack spacing={0.5}>
                    {images.map((file) => (
                      <Typography variant="caption" key={file.name}>
                        {file.name}
                      </Typography>
                    ))}
                  </Stack>
                )}

                <Typography variant="caption" color="text.secondary">
                  최대 6장까지 업로드 가능합니다. (JPG, PNG 형식)
                </Typography>
              </Stack>

              <Stack
                direction={{ xs: "column", sm: "row" }}
                spacing={2}
                justifyContent="flex-end"
              >
                <Button variant="text" onClick={handleReset}>
                  초기화
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  sx={{ minWidth: 180 }}
                  disabled={isSubmitting || !canSubmit}
                >
                  {isSubmitting ? "등록 중..." : "룸메이트 모집하기"}
                </Button>
              </Stack>
            </Stack>
          </Paper>
        </Stack>
      </Container>
      <SiteFooter />
    </Box>
  );
}

// ------------------------------
// Components
// ------------------------------
function SectionTitle({
  icon,
  title,
}: {
  icon?: React.ReactNode;
  title: string;
}) {
  return (
    <Stack direction="row" spacing={1} alignItems="center">
      {icon}
      <Typography variant="h6" fontWeight={700}>
        {title}
      </Typography>
    </Stack>
  );
}

function CheckboxGroup({
  options,
  selected,
  onToggle,
}: {
  options: string[];
  selected: string[];
  onToggle: (option: string) => void;
}) {
  return (
    <Grid container spacing={1.5}>
      {options.map((option) => (
        <Grid item xs={12} sm={6} md={3} key={option}>
          <FormControlLabel
            control={
              <Checkbox
                checked={selected.includes(option)}
                onChange={() => onToggle(option)}
              />
            }
            label={option}
          />
        </Grid>
      ))}
    </Grid>
  );
}
