import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Box, CircularProgress, Alert, Paper, Select, MenuItem, Slider, TextField, Button, Typography, SelectChangeEvent, List, ListItem, ListItemAvatar, Avatar, ListItemText, Divider, ListItemButton, Modal } from "@mui/material";
import SiteHeader from "../components/SiteHeader";
import { api } from "../lib/api";
import type { RoomSummary } from "../types/room";
import { mapRoomFromApi, resolveRoomImageUrl } from "../types/room";

declare global {
  interface Window {
    kakao: any;         // 카카오맵 SDK가 타입스크립트용 타입 정의를 제공하지 않기 때문에 any 사용
    navigateToRoomDetail?: (roomId: number) => void;
  }
}

const roomTypes = [
  { value: "ALL", label: "전체" },
  { value: "ONE_ROOM", label: "원룸" },
  { value: "TWO_ROOM", label: "투룸" },
  { value: "OFFICETEL", label: "오피스텔" },
  { value: "APARTMENT", label: "아파트" },
  { value: "ETC", label: "기타" },
];

const RoomMap: React.FC = () => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);       // 지도 인스턴스를 저장할 ref
  const clustererRef = useRef<any>(null);         // 클러스터러 인스턴스를 저장할 ref
  const location = useLocation();                 // 현재 경로 정보를 가져옵니다.
  const navigate = useNavigate();                 // useNavigate 훅 추가
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rooms, setRooms] = useState<RoomSummary[]>([]); // 지도에 표시될 방 목록 상태 추가
  const infoWindowRef = useRef<any>(null);        // 인포윈도우 인스턴스를 저장할 ref
  // 필터 상태
  const [type, setType] = useState<string>("ALL");
  const [priceRange, setPriceRange] = useState<number[]>([0, 100]); // 예: 0만원 ~ 100만원
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false); // 필터 모달 표시 여부 상태

  const handleTypeChange = (event: SelectChangeEvent<string>) => {
    setType(event.target.value);
  };

  const handlePriceChange = (event: Event, newValue: number | number[]) => {
    setPriceRange(newValue as number[]);
  };

  // 인포윈도우에서 상세 페이지로 이동하기 위한 전역 함수 정의
  // Kakao Maps InfoWindow의 content는 HTML 문자열이므로, React 컴포넌트의 navigate를 직접 호출할 수 없음
  useEffect(() => {
    window.navigateToRoomDetail = (roomId: number) => {
      navigate(`/rooms/${roomId}`);
    };

    return () => {
      delete window.navigateToRoomDetail; // 컴포넌트 언마운트 시 전역 함수 정리
    };
  }, [navigate]);

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps) {
      setError("카카오맵 SDK를 불러오지 못했습니다.");
      setIsLoading(false);
      return;
    }


    window.kakao.maps.load(() => {
      const mapContainer = mapRef.current;
      if (!mapContainer) return;


      // 1. 사용자의 현재 위치 가져오기
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            const userPosition = new window.kakao.maps.LatLng(latitude, longitude);

            // 2. 지도 생성 및 중심 설정
            const map = new window.kakao.maps.Map(mapContainer, {
              center: userPosition,
              level: 5,
            });
            mapInstanceRef.current = map; // 생성된 지도 인스턴스를 ref에 저장

            // 2-1. 마커 클러스터러 생성
            clustererRef.current = new window.kakao.maps.MarkerClusterer({
              map: map, // 클러스터러를 적용할 지도
              averageCenter: true, // 클러스터 마커의 위치를 마커들의 평균 좌표로 설정
              minLevel: 6, // 클러스터링을 시작할 최소 지도 레벨
            });

            // 현재 위치에 특별한 마커 표시
            new window.kakao.maps.Marker({
              map,
              position: userPosition,
              title: "현재 위치",
            });

            // 4. 지도 이동이 멈추면 주변 방 데이터를 다시 불러오는 이벤트 리스너 추가
            window.kakao.maps.event.addListener(map, 'idle', () => {
              if (mapInstanceRef.current) {
                const center = mapInstanceRef.current.getCenter();
                fetchRoomsNearby(center.getLat(), center.getLng());
              }
            });
            // 3. 현재 위치 기반으로 주변 방 데이터 요청
            fetchRoomsNearby(latitude, longitude);
          },
          () => {
            // 위치 정보 가져오기 실패 시 기본 위치(서울)로 설정
            setError("위치 정보를 가져올 수 없습니다. 기본 위치로 지도를 표시합니다.");
            const defaultLat = 37.5665; // 서울 시청 위도
            const defaultLng = 126.9780; // 서울 시청 경도
            const defaultPosition = new window.kakao.maps.LatLng(defaultLat, defaultLng);
            const map = new window.kakao.maps.Map(mapContainer, {
              center: defaultPosition,
              level: 5,
            });
            mapInstanceRef.current = map; // 생성된 지도 인스턴스를 ref에 저장

            // 2-1. 마커 클러스터러 생성
            clustererRef.current = new window.kakao.maps.MarkerClusterer({
              map: map,
              averageCenter: true,
              minLevel: 6,
            });

            // 4. 지도 이동이 멈추면 주변 방 데이터를 다시 불러오는 이벤트 리스너 추가
            window.kakao.maps.event.addListener(map, 'idle', () => {
              if (mapInstanceRef.current) {
                const center = mapInstanceRef.current.getCenter();
                fetchRoomsNearby(center.getLat(), center.getLng());
              }
            });
            // 기본 위치 주변 방 데이터 요청
            fetchRoomsNearby(defaultLat, defaultLng);
          }
        );
      } else {
        setError("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        setIsLoading(false);
      }
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 최초 렌더링 시에만 지도를 초기화합니다.

  const fetchRoomsNearby = async (lat: number, lng: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const params: any = {
        lat,
        lng,
        radiusKm: 3, // 반경을 3km로 설정합니다.
        minPrice: priceRange[0] * 10000, // API 명세에 맞게 단위를 조정해야 합니다. (예: 만원 -> 원)
        maxPrice: priceRange[1] * 10000,
      };
      if (type !== "ALL") {
        params.type = type; // 'buildingType'을 'type'으로 변경
      }

      const { data } = await api.get("/map/rooms/near", { params });

      console.log("API 응답 데이터:", data); // [추가] API 응답을 콘솔에서 확인

      const roomList: RoomSummary[] = Array.isArray(data) ? data.map(mapRoomFromApi) : [];
      setRooms(roomList); // 방 목록 상태 업데이트

      console.log("매핑된 방 목록:", roomList); // [추가] 매핑된 데이터를 콘솔에서 확인

      updateMarkers(roomList);
    } catch (err) {
      console.error("주변 방 정보 로딩 실패:", err); // [추가] 실제 에러를 콘솔에 출력
      setError("주변 방 정보를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const updateMarkers = (rooms: RoomSummary[]) => {
    if (!clustererRef.current) return;

    // 기존 인포윈도우 닫기
    if (infoWindowRef.current) {
      infoWindowRef.current.close();
      infoWindowRef.current = null;
    }

    console.log(`${rooms.length}개의 마커를 생성합니다.`); // [추가] 생성될 마커 수 확인

    // 방(숙소) 마커에 사용할 커스텀 아이콘을 설정합니다.
    const roomMarkerImageSrc = 'http://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png'; // 주황색 마커 아이콘
    const imageSize = new window.kakao.maps.Size(33, 36); // 마커 이미지의 크기
    const imageOption = { offset: new window.kakao.maps.Point(16, 36) }; // 마커의 좌표에 일치시킬 이미지 안의 좌표

    const roomMarkerImage = new window.kakao.maps.MarkerImage(roomMarkerImageSrc, imageSize, imageOption);


    const markers = rooms.map((room) => {
      if (room.latitude && room.longitude && room.id) { // room.id 추가
        const markerPosition = new window.kakao.maps.LatLng(room.latitude, room.longitude);
        const marker = new window.kakao.maps.Marker({
          position: markerPosition,
          title: room.title,
          image: roomMarkerImage, // 커스텀 마커 아이콘을 적용합니다.
        });

        // 인포윈도우 내용 구성
        const roomImageUrl = room.images?.[0]?.imageUrl ? resolveRoomImageUrl(room.images[0].imageUrl) : 'https://via.placeholder.com/150x80?text=No+Image';
        const infoWindowContent = `
          <div style="padding:5px;font-size:12px;width:150px;text-align:center;">
            <img src="${roomImageUrl}" style="width:100%;height:80px;object-fit:cover;margin-bottom:5px;" onerror="this.onerror=null;this.src='https://via.placeholder.com/150x80?text=No+Image';" />
            <div style="font-weight:bold;margin-bottom:3px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${room.title}</div>
            <div style="margin-bottom:5px;">${room.rentPrice.toLocaleString()}원</div>
            ${room.id ? `<a href="/rooms/${room.id}" style="color:blue;text-decoration:underline;cursor:pointer;" onclick="event.preventDefault(); window.navigateToRoomDetail(${room.id});">상세보기</a>` : ''}
          </div>
        `;

        // 마커에 클릭 이벤트 리스너 추가
        window.kakao.maps.event.addListener(marker, 'click', () => {
          // 기존 인포윈도우가 있다면 닫기
          if (infoWindowRef.current) {
            infoWindowRef.current.close();
          }
          const infowindow = new window.kakao.maps.InfoWindow({
            content: infoWindowContent,
            removable: true, // 닫기 버튼 표시
          });
          infowindow.open(mapInstanceRef.current, marker);
          infoWindowRef.current = infowindow; // 현재 열린 인포윈도우 저장
        });

        return marker;
      }
      return null;
    }).filter((marker): marker is any => marker !== null);

    clustererRef.current.clear();
    clustererRef.current.addMarkers(markers);
  };

  // 지도 컨테이너의 크기 변경을 감지하고 relayout을 호출하는 useEffect
  useEffect(() => {
    const mapContainer = mapRef.current;
    if (!mapContainer) return;

    // ResizeObserver를 생성하고 콜백 함수를 정의합니다.
    const observer = new ResizeObserver(() => {
      if (mapInstanceRef.current) {
        // 지도 컨테이너 크기가 변경될 때마다 relayout 함수를 호출합니다.
        mapInstanceRef.current.relayout();
      }
    });

    // mapContainer에 대한 관찰을 시작합니다.
    observer.observe(mapContainer);

    // 컴포넌트가 언마운트될 때 관찰을 중단합니다.
    return () => observer.disconnect();
  }, []);

  const handleApplyFilter = () => {
    if (!mapInstanceRef.current) return;
    const center = mapInstanceRef.current.getCenter();
    fetchRoomsNearby(center.getLat(), center.getLng());
    setIsFilterModalOpen(false); // 필터 적용 후 모달을 닫습니다.
  };

  const handleResetFilter = () => {
    // 상태를 먼저 업데이트하고, 콜백 함수에서 fetch를 호출하여 비동기 문제를 해결합니다.
    setType("ALL");
    setPriceRange([0, 100]);
    setIsFilterModalOpen(false);
    // 이 경우, 상태가 즉시 반영되지 않아도 다음 렌더링 사이클에서 반영되므로,
    // 적용 버튼을 누르거나 지도를 움직이면 초기화된 값으로 검색됩니다.
    // 즉시 반영을 원한다면 fetchRoomsNearby에 초기화된 값을 직접 넘겨야 합니다.
    if (mapInstanceRef.current) {
      const center = mapInstanceRef.current.getCenter();
      // fetchRoomsNearby를 호출하되, 파라미터를 직접 지정해줍니다.
      // 하지만 현재 fetchRoomsNearby는 전역 상태를 사용하므로,
      // 이 방법보다는 '적용'을 누르도록 유도하는 것이 더 간단합니다.
      // 여기서는 단순히 상태만 초기화하고 모달을 닫습니다.
      // 사용자가 지도를 움직이거나 '적용'을 다시 누르면 초기화된 값이 반영됩니다.
    }
  };

  const handleSearch = () => {
    if (!searchQuery || !window.kakao) return;

    const ps = new window.kakao.maps.services.Places();
    ps.keywordSearch(searchQuery, (data: any, status: any) => {
      if (status === window.kakao.maps.services.Status.OK) {
        const newPos = new window.kakao.maps.LatLng(data[0].y, data[0].x);
        mapInstanceRef.current.setCenter(newPos);
        fetchRoomsNearby(newPos.getLat(), newPos.getLng());
        setSearchQuery(""); // 검색 후 검색창 내용 초기화
      } else {
        setError("검색 결과가 없습니다.");
      }
    });
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const modalStyle = {
    position: 'absolute' as 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    boxShadow: 24,
    p: 4,
  };


  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
      <Box
        component="main"
        sx={{
          display: 'flex',
          flexDirection: 'row',
          height: "calc(100vh - 65px)", // 전체 화면 높이에서 헤더 높이(약 65px)를 뺌
        }}
      >
        {/* 왼쪽 패널 */}
        <Paper
          elevation={3}
          sx={{
            width: 400,
            flexShrink: 0,
            display: 'flex',
            flexDirection: 'column',
            p: 2,
            overflowY: 'hidden', // 전체 패널 스크롤 방지
          }}
        >
          {/* 지역 검색 */}
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField label="지역 검색" variant="outlined" size="small" fullWidth value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} onKeyPress={handleKeyPress} />
            <Button variant="contained" onClick={handleSearch} sx={{ whiteSpace: 'nowrap' }}>검색</Button>
          </Box>

          {/* 주변 방 목록 헤더 및 필터 버튼 */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2, mb: 1 }}>
            <Typography variant="h6">주변 방 목록</Typography>
            <Button variant="outlined" size="small" onClick={() => setIsFilterModalOpen(true)}>
              필터
            </Button>
          </Box>

          {/* 주변 방 목록 */}
          <Box sx={{ flex: '1 1 0', minHeight: 0, overflowY: 'auto', borderTop: '1px solid #eee', pt: 1 }}>
            <List dense>
              {rooms.length === 0 ? (
                <ListItem key="no-rooms-found">
                  <ListItemText primary="주변에 방이 없습니다." secondary="지도를 이동하거나 필터를 변경해보세요." />
                </ListItem>
              ) : (
                rooms.map((room) => (
                  <React.Fragment key={room.id}>
                    <ListItem disablePadding>
                      <ListItemButton onClick={() => room.id && navigate(`/rooms/${room.id}`)}>
                        <ListItemAvatar>
                          <Avatar variant="rounded" src={resolveRoomImageUrl(room.images?.[0]?.imageUrl)} alt={room.title} sx={{ width: 56, height: 56, mr: 1 }} />
                        </ListItemAvatar>
                        <ListItemText primary={room.title} secondary={`${room.rentPrice.toLocaleString()}원 | ${room.address}`} />
                      </ListItemButton>
                    </ListItem>
                    <Divider component="li" />
                  </React.Fragment>
                ))
              )}
            </List>
          </Box>
        </Paper>
        {/* 오른쪽 지도 영역 */}
        <Box ref={mapRef} sx={{ width: '100%', height: '100%' }} />
        {(isLoading || error) && (
          <Box position="absolute" top={16} right={16} p={2} zIndex={1000}>
            {isLoading && <CircularProgress />}
            {error && <Alert severity="warning" onClose={() => setError(null)}>{error}</Alert>}
          </Box>
        )}
        {/* 필터 모달 */}
        <Modal
          open={isFilterModalOpen}
          onClose={() => setIsFilterModalOpen(false)}
          aria-labelledby="filter-modal-title"
        >
          <Box sx={modalStyle}>
            <Typography id="filter-modal-title" variant="h6" component="h2" sx={{ mb: 2 }}>
              필터
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box>
                <Typography gutterBottom>타입</Typography>
                <Select value={type} onChange={handleTypeChange} fullWidth size="small" displayEmpty>
                  {roomTypes.map((roomType) => (
                    <MenuItem key={roomType.value} value={roomType.value}>
                      {roomType.label}
                    </MenuItem>
                  ))}
                </Select>
              </Box>
              <Box>
                <Typography gutterBottom>가격 범위 (만원)</Typography>
                <Slider
                  value={priceRange}
                  onChange={handlePriceChange}
                  valueLabelDisplay="auto"
                  min={0}
                  max={100}
                  marks={[{ value: 0, label: '0' }, { value: 100, label: '100+' }]}
                />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, mt: 2 }}>
                <Button variant="outlined" onClick={handleResetFilter} fullWidth>초기화</Button>
                <Button variant="contained" color="primary" onClick={handleApplyFilter} fullWidth>적용</Button>
              </Box>
            </Box>
          </Box>
        </Modal>
      </Box>
    </Box>
  );
};

export default RoomMap;
