import React, { useEffect, useRef, useState, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  CircularProgress,
  Alert,
  Paper,
  Select,
  MenuItem,
  Slider,
  TextField,
  Button,
  Typography,
  SelectChangeEvent,
  List,
  ListItem,
  ListItemAvatar,
  Avatar,
  ListItemText,
  Divider,
  ListItemButton,
  Stack,
  Chip,
  Popover,
  IconButton,
} from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import SiteHeader from "../components/SiteHeader";
import { api, getAccessToken } from "../lib/api";
import type { RoomSummary } from "../types/room";
import { mapRoomFromApi, resolveRoomImageUrl } from "../types/room";

declare global {
  interface Window {
    kakao: any;
  }
}

const roomTypeOptions = [
  { value: "", label: "전체" },
  { value: "ONE_ROOM", label: "원룸" },
  { value: "TWO_ROOM", label: "투룸" },
  { value: "OFFICETEL", label: "오피스텔" },
  { value: "APARTMENT", label: "아파트" },
];

const filterFacilities = [
  "에어컨", "냉장고", "세탁기", "인터넷", "주차장",
  "헬스장", "반려동물 가능", "발코니",
];

const RoomMap: React.FC = () => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rooms, setRooms] = useState<RoomSummary[]>([]);

  const defaultPriceRange: [number, number] = [0, 2000000];

  // Final filter states
  const [roomType, setRoomType] = useState<string>("");
  const [priceRange, setPriceRange] = useState<number[]>(defaultPriceRange);
  const [facilities, setFacilities] = useState<Set<string>>(new Set());
  
  // Temporary states for Popover
  const [tempRoomType, setTempRoomType] = useState(roomType);
  const [tempPriceRange, setTempPriceRange] = useState<number[]>(priceRange);
  const [tempFacilities, setTempFacilities] = useState<Set<string>>(facilities);

  // Other UI states
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);
  const [filterAnchorEl, setFilterAnchorEl] = useState<HTMLButtonElement | null>(null);

  const fetchRoomsNearby = useCallback(async (lat: number, lng: number) => {
    setIsLoading(true);
    setError(null);
    try {
      const params: Record<string, any> = {
        lat, lng, radiusKm: 3,
        minPrice: priceRange[0],
        maxPrice: priceRange[1] === defaultPriceRange[1] ? 5000000 : priceRange[1],
      };
      if (roomType) params.type = roomType;
      if (facilities.size > 0) params.options = Array.from(facilities);

      const token = getAccessToken();
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      const { data } = await api.get("/map/rooms/near", { params, headers });

      const rawRoomList: RoomSummary[] = Array.isArray(data) ? data.map(mapRoomFromApi) : [];
      const uniqueRooms = Array.from(new Map(rawRoomList.map(room => [room.id, room])).values());
      const roomList = uniqueRooms.filter(room => room.id != null);
      setRooms(roomList);
    } catch (err) {
      console.error("주변 방 정보 로딩 실패:", err);
      setError("주변 방 정보를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  }, [priceRange, roomType, facilities, defaultPriceRange]);

  // Effect to fetch rooms when final filter states change
  useEffect(() => {
    if (mapInstanceRef.current) {
        const center = mapInstanceRef.current.getCenter();
        fetchRoomsNearby(center.getLat(), center.getLng());
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [priceRange, roomType, facilities]);

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps) {
      setError("카카오맵 SDK를 불러오지 못했습니다.");
      setIsLoading(false);
      return;
    }
    const kakao = window.kakao;

    kakao.maps.load(() => {
      const mapContainer = mapRef.current;
      if (!mapContainer) return;

      const displayMap = (lat: number, lng: number, onIdle: () => void) => {
        const position = new kakao.maps.LatLng(lat, lng);
        const map = new kakao.maps.Map(mapContainer, { center: position, level: 5 });
        mapInstanceRef.current = map;
        kakao.maps.event.addListener(map, 'idle', onIdle);
        onIdle(); // Initial fetch
        return map;
      };

      const onMapIdle = () => {
        if (mapInstanceRef.current) {
            const center = mapInstanceRef.current.getCenter();
            fetchRoomsNearby(center.getLat(), center.getLng());
        }
      };

      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            const map = displayMap(latitude, longitude, onMapIdle);
            new kakao.maps.Marker({ map, position: new kakao.maps.LatLng(latitude, longitude), title: "현재 위치" });
          },
          () => {
            setError("위치 정보를 가져올 수 없습니다. 기본 위치로 지도를 표시합니다.");
            displayMap(37.5665, 126.9780, onMapIdle);
          }
        );
      } else {
        setError("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        displayMap(37.5665, 126.9780, onMapIdle);
      }
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only on initial mount

  const markers = useMemo(() => {
    const distanceThreshold = 30;
    const clusters: RoomSummary[][] = [];
    const clusteredRoomIds = new Set<number>();
    const getDistanceInMeters = (lat1: number, lon1: number, lat2: number, lon2: number) => {
      const R = 6371e3;
      const φ1 = lat1 * Math.PI/180; const φ2 = lat2 * Math.PI/180;
      const Δφ = (lat2-lat1) * Math.PI/180; const Δλ = (lon2-lon1) * Math.PI/180;
      const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      return R * c;
    };

    rooms.forEach(room => {
      if (!room.id || clusteredRoomIds.has(room.id) || !room.latitude || !room.longitude) return;
      const currentCluster: RoomSummary[] = [room];
      clusteredRoomIds.add(room.id);
      rooms.forEach(targetRoom => {
        if (!targetRoom.id || clusteredRoomIds.has(targetRoom.id) || !targetRoom.latitude || !targetRoom.longitude) return;
        const distance = getDistanceInMeters(room.latitude!, room.longitude!, targetRoom.latitude, targetRoom.longitude);
        if (distance <= distanceThreshold) {
          currentCluster.push(targetRoom);
          clusteredRoomIds.add(targetRoom.id);
        }
      });
      clusters.push(currentCluster);
    });

    return clusters.map(cluster => {
      const rep = cluster[0];
      if (!rep.latitude || !rep.longitude || !rep.id) return null;
      const position = new window.kakao.maps.LatLng(rep.latitude, rep.longitude);
      const isSelected = cluster.some(room => room.id === selectedRoomId);
      const contentText = cluster.length > 1
        ? `${rep.rentPrice.toLocaleString()}원 외 ${cluster.length - 1}개`
        : `${rep.rentPrice.toLocaleString()}원`;
      const content = `<div style="background-color:${isSelected ? '#ff5722' : '#fff'};color:${isSelected ? '#fff' : '#000'};border:1px solid #888;border-radius:4px;padding:4px 8px;font-size:12px;font-weight:bold;white-space:nowrap;cursor:pointer;transition:background-color 0.2s, color 0.2s;">${contentText}</div>`;
      
      const overlay = new window.kakao.maps.CustomOverlay({ position, content, yAnchor: 1 });
      overlay.cluster = cluster;
      overlay.customOnClick = () => handleRoomItemClick(cluster);
      return overlay;
    }).filter((overlay): overlay is any => overlay !== null);
  }, [rooms, selectedRoomId]);

  useEffect(() => {
    if (!mapInstanceRef.current) return;
    const map = mapInstanceRef.current;
    if (!map.customOverlays) map.customOverlays = [];
    map.customOverlays.forEach((o: any) => o.setMap(null));
    map.customOverlays = [];

    markers.forEach((overlay: any) => {
      overlay.setMap(map);
      overlay.a.addEventListener('click', overlay.customOnClick);
      map.customOverlays.push(overlay);
    });
  }, [markers]);
  
  useEffect(() => {
    const observer = new ResizeObserver(() => mapInstanceRef.current?.relayout());
    if(mapRef.current) observer.observe(mapRef.current);
    return () => observer.disconnect();
  }, []);

  const handleOpenFilters = (event: React.MouseEvent<HTMLButtonElement>) => {
    // Sync temp state with final state when opening
    setTempRoomType(roomType);
    setTempPriceRange(priceRange);
    setTempFacilities(new Set(facilities));
    setFilterAnchorEl(event.currentTarget);
  };
  
  const handleCloseFilters = () => {
    setFilterAnchorEl(null);
  };

  const handleApplyFilters = () => {
    setRoomType(tempRoomType);
    setPriceRange(tempPriceRange);
    setFacilities(tempFacilities);
    setSelectedRoomId(null);
    handleCloseFilters();
  };

  const handleResetFiltersInPopover = () => {
    setTempRoomType("");
    setTempPriceRange(defaultPriceRange);
    setTempFacilities(new Set());
  };

  const handleSearch = () => {
    if (!searchQuery || !window.kakao) return;
    new window.kakao.maps.services.Places().keywordSearch(searchQuery, (data: any, status: any) => {
      if (status === window.kakao.maps.services.Status.OK) {
        const newPos = new window.kakao.maps.LatLng(data[0].y, data[0].x);
        setSelectedRoomId(null);
        mapInstanceRef.current.setCenter(newPos);
        setSearchQuery("");
      } else {
        setError("검색 결과가 없습니다.");
      }
    });
  };
  
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => e.key === 'Enter' && handleSearch();
  
  const formatPriceLabel = (value: number) => value >= 2000000 ? '200만+' : (value === 0 ? '0원' : `${value / 10000}만`);

  const handleRoomItemClick = (clusterOrRoom: RoomSummary[] | RoomSummary) => {
    const cluster = Array.isArray(clusterOrRoom) ? clusterOrRoom : [clusterOrRoom];
    const rep = cluster[0];
    if (!mapInstanceRef.current || !rep.latitude || !rep.longitude || !rep.id) return;
    const position = new window.kakao.maps.LatLng(rep.latitude, rep.longitude);
    mapInstanceRef.current.panTo(position);
    setSelectedRoomId(rep.id);
  };

  const displayedRooms = useMemo(() => {
    if (selectedRoomId) {
      const selectedMarker = markers.find(m => m.cluster.some((r: RoomSummary) => r.id === selectedRoomId));
      if (selectedMarker) return selectedMarker.cluster;
      const selectedRoom = rooms.find(r => r.id === selectedRoomId);
      return selectedRoom ? [selectedRoom] : [];
    }
    return rooms;
  }, [rooms, selectedRoomId, markers]);

  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
      <Box component="main" sx={{ display: 'flex', height: "calc(100vh - 65px)" }}>
        <Paper elevation={3} sx={{ width: 400, flexShrink: 0, display: 'flex', flexDirection: 'column', p: 2, overflowY: 'hidden' }}>
          <Stack spacing={2}>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField label="지역/지하철 검색" variant="outlined" size="small" fullWidth value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} onKeyPress={handleKeyPress}/>
              <Button variant="contained" onClick={handleSearch} sx={{ whiteSpace: 'nowrap' }}>검색</Button>
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2, mb: 1 }}>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="h6">주변 방 목록</Typography>
                {displayedRooms.length > 0 && <Chip label={`총 ${displayedRooms.length}개`} size="small" />}
                {selectedRoomId && <Button size="small" variant="text" onClick={() => setSelectedRoomId(null)} sx={{ ml: 1 }}>전체 보기</Button>}
              </Stack>
              <Button variant="outlined" size="small" onClick={handleOpenFilters}>필터</Button>
            </Box>
          </Stack>
          
          <Popover
            open={Boolean(filterAnchorEl)}
            anchorEl={filterAnchorEl}
            onClose={handleCloseFilters}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}
          >
            <Box sx={{ p: 3, width: 350 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h6">필터</Typography>
                    <IconButton onClick={handleCloseFilters} sx={{ p: 0.5 }}><CloseIcon /></IconButton>
                </Box>

                <Stack spacing={3}>
                    <Stack spacing={1}>
                        <Typography variant="subtitle2" color="text.secondary">방 종류</Typography>
                        <Select value={tempRoomType} onChange={(e) => setTempRoomType(e.target.value)} fullWidth size="small" displayEmpty>
                            {roomTypeOptions.map(o => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
                        </Select>
                    </Stack>

                    <Stack spacing={1}>
                        <Typography variant="subtitle2" color="text.secondary">가격 범위</Typography>
                        <Slider value={tempPriceRange} onChange={(_, v) => setTempPriceRange(v as number[])} valueLabelDisplay="auto" valueLabelFormat={formatPriceLabel} min={0} max={2000000} step={10000} />
                        <Typography variant="caption" color="text.secondary">{formatPriceLabel(tempPriceRange[0])} ~ {formatPriceLabel(tempPriceRange[1])}</Typography>
                    </Stack>

                    <Stack spacing={1}>
                        <Typography variant="subtitle2" color="text.secondary">편의시설</Typography>
                        <Stack spacing={1} flexWrap="wrap" direction="row" useFlexGap>
                            {filterFacilities.map(f => (
                                <Chip key={f} label={f}
                                    variant={tempFacilities.has(f) ? "filled" : "outlined"}
                                    color={tempFacilities.has(f) ? "primary" : "default"}
                                    onClick={() => setTempFacilities(prev => { const next = new Set(prev); if (next.has(f)) next.delete(f); else next.add(f); return next; })}
                                    sx={{ borderRadius: 2 }}
                                />
                            ))}
                        </Stack>
                    </Stack>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 1, mt: 2 }}>
                        <Button variant="outlined" onClick={handleResetFiltersInPopover} fullWidth>초기화</Button>
                        <Button variant="contained" color="primary" onClick={handleApplyFilters} fullWidth>적용</Button>
                    </Box>
                </Stack>
            </Box>
          </Popover>

          <Box sx={{ flex: '1 1 0', minHeight: 0, overflowY: 'auto', pt: 1 }}>
            <List dense>
              {displayedRooms.length === 0 ? (
                <ListItem><ListItemText primary="주변에 방이 없습니다." secondary="지도를 이동하거나 필터를 변경해보세요." /></ListItem>
              ) : (
                displayedRooms.map(room => (
                  <React.Fragment key={room.id}>
                    <ListItem disablePadding sx={{ backgroundColor: selectedRoomId === room.id ? 'action.hover' : 'transparent' }}>
                      <ListItemButton onClick={() => navigate(`/rooms/${room.id}`)}>
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

        <Box ref={mapRef} sx={{ width: '100%', height: '100%' }} />

        {(isLoading || error) && (
          <Box position="absolute" top={80} right={16} p={2} zIndex={1000}>
            {isLoading && <CircularProgress />}
            {error && <Alert severity="warning" onClose={() => setError(null)}>{error}</Alert>}
          </Box>
        )}
      </Box>
    </Box>
  );
};

export default RoomMap;
