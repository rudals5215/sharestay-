<<<<<<< HEAD
import React, { useEffect, useRef, useState, useCallback, useMemo } from "react";
=======
import React, {
  useEffect,
  useRef,
  useState,
  useCallback,
  useMemo,
} from "react";
>>>>>>> main
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
  List,
  ListItem,
  ListItemText,
  Divider,
  ListItemButton,
  Stack,
  Chip,
<<<<<<< HEAD
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
=======
  Modal,
  IconButton,
  Fab,
} from "@mui/material";
import SiteHeader from "../components/SiteHeader";
import CloseIcon from "@mui/icons-material/Close";
import MyLocationIcon from "@mui/icons-material/MyLocation";
import { api, getAccessToken } from "../lib/api";
import type { RoomSummary } from "../types/room";
import { mapRoomFromApi, resolveRoomImageUrl, RoomApiResponse } from "../types/room"; // RoomApiResponse 추가
import {
  provinces,
  provinceDistrictMap,
  roomTypeOptions,
  filterFacilities,
} from "../types/filters";
import fallbackImageSrc from "../img/no_img.jpg";
const fallbackImage = fallbackImageSrc;

const RoomMap: React.FC = () => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<kakao.maps.Map | null>(null); // 지도 인스턴스를 저장할 ref
  const geocoderRef = useRef<kakao.maps.services.Geocoder | null>(null); // 지오코더 인스턴스를 저장할 ref
  const navigate = useNavigate(); // useNavigate 훅 추가
>>>>>>> main
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rooms, setRooms] = useState<RoomSummary[]>([]);

  const defaultPriceRange: [number, number] = [0, 2000000];

  // Final filter states
  const [roomType, setRoomType] = useState<string>("");
  const [region, setRegion] = useState<string>(""); // 지역 필터 상태 추가
  const [district, setDistrict] = useState<string>(""); // 시/군/구 필터 상태 추가
  const [priceRange, setPriceRange] = useState<number[]>(defaultPriceRange);
  const [facilities, setFacilities] = useState<Set<string>>(new Set());
<<<<<<< HEAD
  
  // Temporary states for Popover
  const [tempRoomType, setTempRoomType] = useState(roomType);
  const [tempPriceRange, setTempPriceRange] = useState<number[]>(priceRange);
  const [tempFacilities, setTempFacilities] = useState<Set<string>>(facilities);
=======
  const [isFilterModalOpen, setIsFilterModalOpen] = useState(false);
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null); // 선택된 방 ID 상태
  const [hoveredRoomId, setHoveredRoomId] = useState<number | null>(null); // 마우스 오버된 방 ID 상태
  const highlightOverlayRef = useRef<kakao.maps.CustomOverlay | null>(null); // 강조 효과 오버레이를 관리하기 위한 ref
  const districtOptions = useMemo(() => {
    return region ? provinceDistrictMap[region] ?? [] : [];
  }, [region]);

  const [modalRoom, setModalRoom] = useState<RoomSummary | null>(null);

  const handleToggleFacility = (facility: string) => {
    setFacilities((prev) => {
      const next = new Set(prev);
      if (next.has(facility)) next.delete(facility);
      else next.add(facility);
      return next;
    });
  };
>>>>>>> main

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

<<<<<<< HEAD
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
=======
      // 1. 사용자의 현재 위치 가져오기
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
          const { latitude, longitude } = position.coords;
          const userPosition = new window.kakao.maps.LatLng(
            latitude,
            longitude
          );

          const map = new window.kakao.maps.Map(mapContainer, {
              center: userPosition,
              level: 4,
            });
            mapInstanceRef.current = map; // 생성된 지도 인스턴스를 ref에 저장
            geocoderRef.current = new window.kakao.maps.services.Geocoder(); // 지오코더 인스턴스 생성

            // 지도 이동이 멈추면 주변 방 데이터를 다시 불러오는 이벤트 리스너 추가
            window.kakao.maps.event.addListener(map, "idle", () => {
              if (mapInstanceRef.current) { // mapInstanceRef.current가 null이 아님을 보장
                const map = mapInstanceRef.current;
                const center = map.getCenter();
                const level = map.getLevel();
                fetchRoomsNearby(center.getLat(), center.getLng(), level);
              }
            });
            // 현재 위치 기반으로 주변 방 데이터 요청
            fetchRoomsNearby(latitude, longitude, map.getLevel());
          },
          () => {
            // 위치 정보 가져오기 실패 시 기본 위치(서울)로 설정
            setError(
              "위치 정보를 가져올 수 없습니다. 기본 위치로 지도를 표시합니다."
            );
            const defaultLat = 37.5665; // 서울 시청 위도
            const defaultLng = 126.978; // 서울 시청 경도
            const defaultPosition = new window.kakao.maps.LatLng(
              defaultLat,
              defaultLng
            );
            const map = new window.kakao.maps.Map(mapContainer, {
              center: defaultPosition,
              level: 4,
            }); // mapInstanceRef.current가 null이 아님을 보장
            mapInstanceRef.current = map;
            geocoderRef.current = new window.kakao.maps.services.Geocoder(); // 지오코더 인스턴스 생성
>>>>>>> main
          }
        );
      } else {
        setError("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        displayMap(37.5665, 126.9780, onMapIdle);
      }
    });
<<<<<<< HEAD
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only on initial mount

  const markers = useMemo(() => {
=======
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // 최초 렌더링 시에만 지도를 초기화합니다.

  const fetchRoomsNearby = useCallback(
    async (lat: number, lng: number, level: number) => {
      setIsLoading(true);
      setError(null);
      try {
        // 지도 중심 주소 가져오기 (비동기)
        const getCenterAddress = (): Promise<string | null> => { // mapInstanceRef.current가 null이 아님을 보장
          if (!mapInstanceRef.current || !geocoderRef.current) return Promise.resolve(null);
          return new Promise((resolve) => {
            const center = mapInstanceRef.current.getCenter();
            geocoderRef.current!.coord2RegionCode(
              center.getLng(),
              center.getLat(),
              (result, status) => { // result: { region_type: 'H' | 'B'; ... }[]
                if (status === window.kakao.maps.services.Status.OK) {
                  // '구' 단위 주소(예: 강남구)를 찾아서 반환
                  const regionInfo = result.find((r) => r.region_type === 'H');
                  resolve(regionInfo ? regionInfo.region_2depth_name : null);
                } else {
                  resolve(null);
                }
              }
            );
          });
        };

        // 현재 지도 화면의 사각 경계를 가져옵니다.
        if (!mapInstanceRef.current) return; // mapInstanceRef.current가 null이 아님을 보장
        const bounds = mapInstanceRef.current.getBounds();
        const sw = bounds.getSouthWest(); // 남서쪽 좌표
        const center = mapInstanceRef.current!.getCenter();
        const ne = bounds.getNorthEast(); // 북동쪽 좌표

        // Haversine 공식을 사용하여 지도 중심에서 모서리까지의 거리를 계산합니다.
        const R = 6371; // 지구의 반지름 (km)
        const lat1 = center.getLat() * (Math.PI / 180);
        const lat2 = ne.getLat() * (Math.PI / 180);
        const deltaLat = (ne.getLat() - center.getLat()) * (Math.PI / 180);
        const deltaLng = (ne.getLng() - center.getLng()) * (Math.PI / 180);

        const a =
          Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
          Math.cos(lat1) *
            Math.cos(lat2) *
            Math.sin(deltaLng / 2) *
            Math.sin(deltaLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        const radiusKm = R * c; // 계산된 반경 (km)

        const params: Record<string, string | number | string[] | undefined> = {
          swLat: sw.getLat(),
          swLng: sw.getLng(),
          neLat: ne.getLat(),
          neLng: ne.getLng(),
          minPrice: priceRange[0],
          maxPrice: priceRange[1],
          level, // API에 지도 레벨도 전달
        };
        if (roomType) {
          params.type = roomType;
        }
        if (region) {
          params.region = region;
        }
        if (district) {
          params.district = district;
        }
        if (facilities.size > 0) {
          params.options = Array.from(facilities);
        }

        const token = getAccessToken();
        const headers = token ? { Authorization: `Bearer ${token}` } : {};
        const { data } = await api.get("/map/rooms/near", {
          params,
          headers,
        });

        console.log("API 응답 데이터:", data); // [추가] API 응답을 콘솔에서 확인

        // API 응답이 배열이거나, data 또는 result 프로퍼티에 배열이 담겨오는 경우를 모두 처리합니다.
        const roomData = Array.isArray(data)
          ? data
          : data?.data && Array.isArray(data.data)
          ? data.data
          : data?.result ?? [];

        const rawRoomList: RoomSummary[] = Array.isArray(roomData) // apiRoom의 타입은 RoomApiResponse
          ? roomData.map((apiRoom: any, index) => {
              const room = mapRoomFromApi(apiRoom);
              // mapRoomFromApi에서 roomId가 매핑되지 않는 경우를 대비해 직접 할당합니다.
              if (room.roomId === undefined && apiRoom.roomId !== undefined) {
                room.roomId = apiRoom.roomId;
                room.id = apiRoom.roomId;
                room.totalMembers = apiRoom.availabilityStatus;
                room.hostId = apiRoom.hostId;
              }

              return room;
            })
          : [];
          
          // 주소는 있지만 좌표가 없는 방들을 지오코딩합니다. (latitude 또는 longitude가 없는 경우)
          const geocodingPromises = rawRoomList
          .filter((room) => room.address && (!room.latitude || !room.longitude))
          .map((room) => {
            return new Promise<RoomSummary>((resolve) => {
              if (!geocoderRef.current) {
                resolve(room); // 지오코더가 없으면 원본 방 정보 반환
                return;
              }
              geocoderRef.current!.addressSearch(
                room.address,
                (result, status) => { // result: { x: string, y: string }[]
                  if (status === window.kakao.maps.services.Status.OK) {
                    // 검색 성공 시, 좌표를 추가하여 반환
                    resolve({
                      ...room,
                      latitude: parseFloat(result[0].y),
                      longitude: parseFloat(result[0].x),
                    });
                  } else {
                    // 검색 실패 시, 원본 방 정보 반환
                    resolve(room);
                  }
                }
              );
            });
          });

        const geocodedRooms = await Promise.all(geocodingPromises);
        console.log(
          "Geocoded Rooms (with new coords):",
          geocodedRooms.filter((r) => r.latitude && r.longitude)
        );

        // ID를 기준으로 중복된 방을 제거합니다.
        const uniqueRooms = Array.from(
          new Map(
            [...rawRoomList, ...geocodedRooms]
              .filter((room) => room.id)
              .map((room) => [room.id, room])
          ).values()
        );

        setRooms(uniqueRooms);
        console.log("매핑된 방 목록:", uniqueRooms);
      } catch (err) {
        console.error("주변 방 정보 로딩 실패:", err); // [추가] 실제 에러를 콘솔에 출력
        setError("주변 방 정보를 불러오는 중 오류가 발생했습니다.");
      } finally {
        setIsLoading(false); // 로딩 상태를 finally 블록에서 해제
      }
    },
    [priceRange, roomType, region, district, facilities] // 의존성 배열에 region, district 추가
  );

  const handleRoomItemClick = useCallback(
    (clusterOrRoom: RoomSummary[] | RoomSummary) => {
      const cluster = Array.isArray(clusterOrRoom)
        ? clusterOrRoom
        : [clusterOrRoom];
      const representativeRoom = cluster[0];

      if (!representativeRoom?.id) return;

      // 이미 선택된 마커를 다시 클릭하면 선택 해제
      if (selectedRoomId === representativeRoom.id) {
        setSelectedRoomId(null);
      } else {
        // 새로운 마커를 클릭하면 선택
        setSelectedRoomId(representativeRoom.id);
      }
    },
    [selectedRoomId] // selectedRoomId가 변경될 때마다 함수를 새로 만들어 최신 상태를 참조하도록 합니다.
  );

  const handleGoToMyLocation = useCallback(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          const myPosition = new window.kakao.maps.LatLng(latitude, longitude);
          const map = mapInstanceRef.current;
          if (map) {
            map.panTo(myPosition);
            // 지도 이동 후, 해당 위치의 방들을 즉시 검색합니다.
            fetchRoomsNearby(latitude, longitude, map.getLevel());
          }
        },
        () => {
          alert("현재 위치를 가져올 수 없습니다.");
        }
      );
    } else {
      alert("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
    }
  }, [fetchRoomsNearby]);

  // rooms 배열이 변경될 때만 마커를 다시 생성합니다.
  const markers = useMemo(() => {
    // 1. 그룹화 기준 거리를 30m로 고정
>>>>>>> main
    const distanceThreshold = 30;
    const clusters: RoomSummary[][] = [];
    const clusteredRoomIds = new Set<number>();
<<<<<<< HEAD
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
=======

    // 좌표가 있는 방만 필터링합니다.
    const roomsWithCoords = rooms.filter(
      (room) => room.latitude && room.longitude
    );
    console.log(
      "Rooms with valid coordinates for clustering:",
      roomsWithCoords
    );

    const getDistanceInMeters = (
      lat1: number,
      lon1: number,
      lat2: number,
      lon2: number
    ) => {
      const R = 6371e3; // metres
      const φ1 = (lat1 * Math.PI) / 180;
      const φ2 = (lat2 * Math.PI) / 180;
      const Δφ = ((lat2 - lat1) * Math.PI) / 180;
      const Δλ = ((lon2 - lon1) * Math.PI) / 180;
      const a =
        Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    };

    for (const room of roomsWithCoords) {
      // 필터링된 방 목록 사용
      if (room.id && !clusteredRoomIds.has(room.id)) {
        const currentCluster: RoomSummary[] = [room];
        clusteredRoomIds.add(room.id);

        for (const targetRoom of rooms) {
          if (
            targetRoom.id &&
            !clusteredRoomIds.has(targetRoom.id) &&
            room.latitude &&
            room.longitude &&
            targetRoom.latitude &&
            targetRoom.longitude
          ) {
            const distance = getDistanceInMeters(
              room.latitude,
              room.longitude,
              targetRoom.latitude,
              targetRoom.longitude
            );

            if (distance <= distanceThreshold) {
              currentCluster.push(targetRoom);
              clusteredRoomIds.add(targetRoom.id);
            }
          }
>>>>>>> main
        }
        clusters.push(currentCluster);
      }
    }

<<<<<<< HEAD
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
=======
    // 3. 클러스터/단일 오버레이 생성
    return clusters
      .map((cluster) => {
        const representativeRoom = cluster[0]; // 클러스터의 첫 번째 방을 대표로 사용
        if (
          !representativeRoom.latitude ||
          !representativeRoom.longitude ||
          !representativeRoom.id
        )
          return null;

        const position = new window.kakao.maps.LatLng(
          representativeRoom.latitude,
          representativeRoom.longitude
        );
        let contentText = "";
        const isSelected = cluster.some((room) => room.id === selectedRoomId);
        const isHovered = cluster.some((room) => room.id === hoveredRoomId);

        if (cluster.length > 1) {
          const otherCount = cluster.length - 1;
          contentText = `${representativeRoom.rentPrice.toLocaleString()}원 외 ${otherCount}개`;
        } else {
          contentText = `${representativeRoom.rentPrice.toLocaleString()}원`;
        }

        // 하이라이트 효과를 원으로 대체하므로, 마커 자체의 스타일은 선택 여부에 따라 최소한으로 변경하거나 고정합니다.
        const color = isSelected ? "#ff5722" : "#000";
        const fontWeight = isSelected ? "900" : "bold";
        const content = `<div style="background-color:#fff;color:${color};border:1px solid ${color};border-radius:4px;padding:4px 8px;font-size:12px;font-weight:${fontWeight};white-space:nowrap;cursor:pointer;transition:all 0.2s;">${contentText}</div>`;

        const contentNode = document.createElement("div");
        contentNode.innerHTML = content;
        contentNode.onclick = () => handleRoomItemClick(cluster);
        contentNode.onmouseover = () => setHoveredRoomId(representativeRoom.id);
        contentNode.onmouseout = () => setHoveredRoomId(null);

        const overlay = new window.kakao.maps.CustomOverlay({
          position,
          content: contentNode,
          yAnchor: 1,
        });
        overlay.cluster = cluster;
        console.log(
          "Created CustomOverlay for room/cluster:",
          representativeRoom.id,
          representativeRoom.title
        );
        return overlay;
      })
      .filter((overlay): overlay is kakao.maps.CustomOverlay & { cluster: RoomSummary[] } => overlay !== null);
  }, [rooms, selectedRoomId, hoveredRoomId, handleRoomItemClick]);
>>>>>>> main

  useEffect(() => {
    if (!mapInstanceRef.current) return;
    const map = mapInstanceRef.current;
<<<<<<< HEAD
    if (!map.customOverlays) map.customOverlays = [];
    map.customOverlays.forEach((o: any) => o.setMap(null));
    map.customOverlays = [];
=======

    // if (!map.customOverlays) map.customOverlays = []; // map.customOverlays가 undefined일 경우 초기화
    // map.customOverlays.forEach((overlay) => overlay.setMap(null));
    // map.customOverlays = [];
>>>>>>> main

    markers.forEach((overlay) => {
      overlay.setMap(map);
      // map.customOverlays.push(overlay);
    });
  }, [markers]);
<<<<<<< HEAD
  
=======

  // Pulsing animation을 위한 keyframes를 style 태그로 주입
  useEffect(() => {
    const styleId = "pulsing-animation-style";
    if (!document.getElementById(styleId)) {
      const style = document.createElement("style");
      style.id = styleId;
      style.innerHTML = `
        @keyframes pulse {
          0% { transform: scale(0.95); opacity: 0.7; }
          70% { transform: scale(1.4); opacity: 0; }
          100% { transform: scale(0.95); opacity: 0; }
        }
      `;
      document.head.appendChild(style);
    }
  }, []);

  // 선택 또는 호버 변경 시 하이라이트 원을 업데이트하는 useEffect
  useEffect(() => {
    if (!mapInstanceRef.current) return;

    // 이전 하이라이트가 있으면 먼저 제거
    if (highlightOverlayRef.current) {
      highlightOverlayRef.current.setMap(null);
    }

    const highlightId = selectedRoomId ?? hoveredRoomId;
    if (!highlightId) {
      return;
    }

    // 하이라이트할 방 찾기
    const roomToHighlight = rooms.find((room) => room.id === highlightId);

    if (roomToHighlight?.latitude && roomToHighlight?.longitude) {
      const isSelected = roomToHighlight.id === selectedRoomId;

      const content = document.createElement("div");
      content.style.width = "100px";
      content.style.height = "30px";
      content.style.borderRadius = "8px"; // 둥근 사각형
      content.style.backgroundColor = isSelected
        ? "rgba(255, 87, 34, 0.3)"
        : "rgba(128, 128, 128, 0.4)";
      content.style.animation = "pulse 1.5s infinite ease-out";

      const newHighlightOverlay = new window.kakao.maps.CustomOverlay({
        position: new window.kakao.maps.LatLng(
          roomToHighlight.latitude,
          roomToHighlight.longitude
        ),
        content: content,
        yAnchor: 1, // 마커와 동일한 yAnchor로 위치 보정
        xAnchor: 0.5,
        zIndex: -1, // 마커 뒤에 표시되도록 z-index 설정
      });

      newHighlightOverlay.setMap(mapInstanceRef.current);
      highlightOverlayRef.current = newHighlightOverlay;
    }
  }, [selectedRoomId, hoveredRoomId, rooms]); // rooms가 변경될 때도 원을 다시 그려야 할 수 있음

  // 지도 컨테이너 resize 대응
>>>>>>> main
  useEffect(() => {
    const observer = new ResizeObserver(() => mapInstanceRef.current?.relayout());
    if(mapRef.current) observer.observe(mapRef.current);
    return () => observer.disconnect();
  }, []);

<<<<<<< HEAD
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
=======
  const handleApplyFilter = () => {
    if (!mapInstanceRef.current) return;
    const map = mapInstanceRef.current;
    const center = map.getCenter();
    setSelectedRoomId(null); // 필터 적용 시 선택 해제
    fetchRoomsNearby(center.getLat(), center.getLng(), map.getLevel());
    setIsFilterModalOpen(false); // 필터 적용 후 모달을 닫습니다.
  };

  const handleResetFilter = () => {
    setRoomType("");
    setRegion(""); // 지역 필터 초기화
    setDistrict(""); // 시/군/구 필터 초기화
    setPriceRange(defaultPriceRange);
    setFacilities(new Set());
    setSelectedRoomId(null); // 필터 초기화 시 선택 해제
    // 상태 변경 후 useEffect가 데이터 fetching을 처리하므로 모달만 닫습니다.
    setIsFilterModalOpen(false);
>>>>>>> main
  };

  const handleSearch = () => {
    if (!searchQuery || !window.kakao) return;
<<<<<<< HEAD
    new window.kakao.maps.services.Places().keywordSearch(searchQuery, (data: any, status: any) => {
      if (status === window.kakao.maps.services.Status.OK) {
        const newPos = new window.kakao.maps.LatLng(data[0].y, data[0].x);
        setSelectedRoomId(null);
        mapInstanceRef.current.setCenter(newPos);
        setSearchQuery("");
      } else {
        setError("검색 결과가 없습니다.");
=======

    new window.kakao.maps.services.Places().keywordSearch( // eslint-disable-line
      searchQuery,
      (data, status) => { // data: { x: string, y: string }[]
        if (status === window.kakao.maps.services.Status.OK) {
          const map = mapInstanceRef.current;
          const newPos = new window.kakao.maps.LatLng(data[0].y, data[0].x);
          setSelectedRoomId(null); // 새로운 지역 검색 시 선택 해제
          map.setCenter(newPos);
          fetchRoomsNearby(newPos.getLat(), newPos.getLng(), map.getLevel());
          setSearchQuery(""); // 검색 후 검색창 내용 초기화
        } else {
          setError("검색 결과가 없습니다.");
        }
>>>>>>> main
      }
    );
  };
  
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => e.key === 'Enter' && handleSearch();
  
  const formatPriceLabel = (value: number) => value >= 2000000 ? '200만+' : (value === 0 ? '0원' : `${value / 10000}만`);

<<<<<<< HEAD
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
=======
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  // 가격 포맷팅 함수
  const formatPriceLabel = (value: number) => {
    if (value >= 2000000) {
      return "200만+";
    }
    if (value === 0) return "0원";
    return `${value / 10000}만`;
  };

  const getClusterRepresentativeId = (
    clusterOrRoom: RoomSummary[] | RoomSummary
  ) => {
    const cluster = Array.isArray(clusterOrRoom)
      ? clusterOrRoom
      : [clusterOrRoom];
    return cluster[0]?.id ?? null;
  };

  const modalStyle = {
    position: "absolute" as "absolute",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    width: 400,
    bgcolor: "background.paper",
    boxShadow: 24,
    p: 4,
  };

  const RoomDetailModal: React.FC<{
    room: RoomSummary | null;
    onClose: () => void;
    onNavigate: (roomId: number) => void;
  }> = ({ room, onClose, onNavigate }) => {
    if (!room) return null;

    const imageUrl = resolveRoomImageUrl(room.images?.[0]?.imageUrl);

    return (
      <Modal
        open={!!room}
        onClose={onClose}
        aria-labelledby="room-detail-modal-title"
      >
        <Box sx={modalStyle}>
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              mb: 2,
            }}
          >
            <Typography
              id="room-detail-modal-title"
              variant="h6"
              component="h2"
            >
              방 정보 요약
            </Typography>
            <IconButton onClick={onClose} sx={{ p: 0.5 }}>
              <CloseIcon />
            </IconButton>
          </Box>

          <Stack spacing={2}>
            {imageUrl && (
              <Box
                component="img"
                src={imageUrl}
                alt={room.title}
                sx={{
                  width: "100%",
                  height: 200,
                  objectFit: "cover",
                  borderRadius: 2,
                }}
              />
            )}

            <Typography variant="h5" fontWeight={700}>
              {room.title}
            </Typography>
            <Typography variant="h6" color="primary">
              {room.rentPrice.toLocaleString()}원/월
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {room.address}
            </Typography>

            {room.description && (
              <Typography variant="body2" noWrap textOverflow="ellipsis">
                {room.description}
              </Typography>
            )}

            <Stack direction="row" spacing={2} justifyContent="flex-end" mt={2}>
              <Button variant="outlined" onClick={onClose}>
                닫기
              </Button>
              <Button
                variant="contained"
                onClick={() => room.id && onNavigate(room.id)}
                disabled={!room.id}
              >
                상세 페이지로 이동
              </Button>
            </Stack>
          </Stack>
        </Box>
      </Modal>
    );
  };

  // 목록에 표시할 방 목록을 결정합니다.
  const displayedRooms = useMemo(() => {
    if (selectedRoomId) {
      // markers 배열에서 해당 클러스터를 찾습니다.
      const selectedMarker = markers.find((marker: kakao.maps.CustomOverlay & { cluster: RoomSummary[] }) =>
        marker.cluster.some((room) => room.id === selectedRoomId)
      );

      if (selectedMarker) {
        return selectedMarker.cluster;
      }
      // 만약 markers에서 못찾는 경우(엣지 케이스)를 대비해 단일 방이라도 보여줍니다.
      const selectedRoom = rooms.find((room) => room.id === selectedRoomId);
>>>>>>> main
      return selectedRoom ? [selectedRoom] : [];
    }
    return rooms;
  }, [rooms, selectedRoomId, markers]);

  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
<<<<<<< HEAD
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
=======
      <Box
        component="main"
        sx={{
          display: "flex",
          flexDirection: "row",
          height: "calc(100vh - 65px)", // 전체 화면 높이에서 헤더 높이(약 65px)를 뺌
        }}
      >
        {/* 왼쪽 패널 */}
        <Paper
          elevation={3}
          sx={{
            width: 400,
            flexShrink: 0,
            display: "flex",
            flexDirection: "column",
            p: 2,
            overflowY: "hidden", // 전체 패널 스크롤 방지
          }}
        >
          <Stack spacing={2}>
            {/* 지역 검색 */}
            <Box sx={{ display: "flex", gap: 1 }}>
              <TextField
                label="지역/지하철 검색"
                variant="outlined"
                size="small"
                fullWidth
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <Button
                variant="contained"
                onClick={handleSearch}
                sx={{ whiteSpace: "nowrap" }}
              >
                검색
              </Button>
            </Box>

            {/* 주변 방 목록 헤더 및 필터 버튼 */}
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                mt: 2,
                mb: 1,
              }}
            >
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="h6">주변 방 목록</Typography>
                {displayedRooms.length > 0 && (
                  <Chip label={`총 ${displayedRooms.length}개`} size="small" />
                )}
                {selectedRoomId && (
                  <Button
                    size="small"
                    variant="text"
                    onClick={() => setSelectedRoomId(null)}
                    sx={{ ml: 1 }}
                  >
                    전체 보기
                  </Button>
                )}
              </Stack>
              <Button
                variant="outlined"
                size="small"
                onClick={() => setIsFilterModalOpen(true)}
              >
                필터
              </Button>
>>>>>>> main
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

<<<<<<< HEAD
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
=======
          {/* 주변 방 목록 */}
          <Box sx={{ flex: "1 1 0", minHeight: 0, overflowY: "auto", pt: 1 }}>
            <List dense>
              {displayedRooms.length === 0 ? (
                <ListItem key="no-rooms-found">
                  <ListItemText
                    primary="주변에 방이 없습니다."
                    secondary="지도를 이동하거나 필터를 변경해보세요."
                  />
                </ListItem>
              ) : (
                displayedRooms.map((room) => [
                  <ListItem
                    key={room.roomId ?? room.id}
                    id={`room-item-${room.id}`}
                    disablePadding
                    onMouseEnter={() => setHoveredRoomId(room.id)}
                    onMouseLeave={() => setHoveredRoomId(null)}
                    sx={{
                      backgroundColor:
                        selectedRoomId === room.id || hoveredRoomId === room.id
                          ? "action.hover"
                          : "transparent",
                      transition: "background-color 0.3s",
                    }}
                  >
                    <ListItemButton
                      onClick={() => setModalRoom(room)}
                      sx={{
                        borderLeft:
                          hoveredRoomId === room.id
                            ? "4px solid #ffc107"
                            : "none",
                        paddingLeft:
                          hoveredRoomId === room.id ? "12px" : "16px",
                      }}
                    >
                      <Box
                        component="img"
                        src={room.images?.[0]?.imageUrl ?? fallbackImage}
                        alt={room.title}
                        sx={{
                          width: 170,
                          height: 150,
                          borderRadius: 2,
                          objectFit: "cover",
                          mr: 2,
                          flexShrink: 0,
                        }}
                      />
                      <ListItemText
                        primary={room.title}
                        secondary={`${room.rentPrice.toLocaleString()}원 | ${
                          room.address
                        }`}
                      />
                    </ListItemButton>
                  </ListItem>,
                  <Divider key={`divider-${room.id}`} component="li" />,
                ])
>>>>>>> main
              )}
            </List>
          </Box>
        </Paper>
<<<<<<< HEAD

        <Box ref={mapRef} sx={{ width: '100%', height: '100%' }} />

=======
        {/* 오른쪽 지도 영역 */}
        <Box sx={{ width: "100%", height: "100%", position: "relative" }}>
          <Box ref={mapRef} sx={{ width: "100%", height: "100%" }} />
          <Fab
            color="primary"
            aria-label="go to my location"
            sx={{ position: "absolute", bottom: 24, right: 24 }}
            onClick={handleGoToMyLocation}
          >
            <MyLocationIcon />
          </Fab>
        </Box>
>>>>>>> main
        {(isLoading || error) && (
          <Box position="absolute" top={80} right={16} p={2} zIndex={1000}>
            {isLoading && <CircularProgress />}
            {error && (
              <Alert severity="warning" onClose={() => setError(null)}>
                {error}
              </Alert>
            )}
          </Box>
        )}
<<<<<<< HEAD
=======
        <RoomDetailModal
          room={modalRoom}
          onClose={() => setModalRoom(null)}
          onNavigate={(roomId) => navigate(`/rooms/${roomId}`)}
        />
        {/* 필터 모달 */}
        <Modal
          open={isFilterModalOpen}
          onClose={() => setIsFilterModalOpen(false)}
          aria-labelledby="filter-modal-title"
        >
          <Box sx={modalStyle}>
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                mb: 2,
              }}
            >
              <Typography id="filter-modal-title" variant="h6" component="h2">
                필터
              </Typography>
              <IconButton
                onClick={() => setIsFilterModalOpen(false)}
                sx={{ p: 0.5 }}
              >
                <CloseIcon />
              </IconButton>
            </Box>

            <Stack spacing={3}>
              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  지역
                </Typography>
                <Select
                  value={region}
                  onChange={(e) => {
                    setRegion(e.target.value);
                    setDistrict(""); // 지역 변경 시 시/군/구 초기화
                  }}
                  fullWidth
                  size="small"
                  displayEmpty
                >
                  <MenuItem value="">
                    <em>지역 전체</em>
                  </MenuItem>
                  {provinces.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </Stack>

              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  시/군/구
                </Typography>
                <Select
                  value={district}
                  onChange={(e) => setDistrict(e.target.value)}
                  fullWidth
                  size="small"
                  displayEmpty
                  disabled={!region} // 지역이 선택되어야 활성화
                >
                  <MenuItem value="">
                    <em>구/읍/면 전체</em>
                  </MenuItem>
                  {districtOptions.map((option) => (
                    <MenuItem key={option} value={option}>
                      {option}
                    </MenuItem>
                  ))}
                </Select>
              </Stack>
              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  방 종류
                </Typography>
                <Select
                  value={roomType}
                  onChange={(e) => setRoomType(e.target.value)}
                  fullWidth
                  size="small"
                  displayEmpty
                >
                  {roomTypeOptions.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </Stack>

              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  가격 범위
                </Typography>
                <Slider
                  value={priceRange}
                  onChange={handlePriceChange}
                  valueLabelDisplay="auto"
                  valueLabelFormat={formatPriceLabel} // 툴팁 포맷 적용
                  min={0}
                  max={2000000}
                  step={10000}
                />
                <Typography variant="caption" color="text.secondary">
                  {formatPriceLabel(priceRange[0])} ~{" "}
                  {formatPriceLabel(priceRange[1])}
                </Typography>
              </Stack>

              <Stack spacing={1}>
                <Typography variant="subtitle2" color="text.secondary">
                  편의시설
                </Typography>
                <Stack spacing={1} flexWrap="wrap" direction="row" useFlexGap>
                  {filterFacilities.map((facility) => (
                    <Chip
                      key={facility}
                      label={facility}
                      variant={facilities.has(facility) ? "filled" : "outlined"}
                      color={facilities.has(facility) ? "primary" : "default"}
                      onClick={() => handleToggleFacility(facility)}
                      sx={{ borderRadius: 2 }}
                    />
                  ))}
                </Stack>
              </Stack>
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  gap: 1,
                  mt: 2,
                }}
              >
                <Button
                  variant="outlined"
                  onClick={handleResetFilter}
                  fullWidth
                >
                  초기화
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={handleApplyFilter}
                  fullWidth
                >
                  적용
                </Button>
              </Box>
            </Stack>
          </Box>
        </Modal>
>>>>>>> main
      </Box>
      {/* 필요하면 나중에 지도 아래에 리스트 붙일 수 있음 */}
      {/* <SiteFooter /> */}
    </Box>
  );
};

export default RoomMap;
