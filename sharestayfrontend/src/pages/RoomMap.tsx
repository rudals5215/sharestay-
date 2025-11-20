import React, { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { Box, CircularProgress, Alert } from "@mui/material";
import SiteHeader from "../components/SiteHeader";
import SiteFooter from "../components/SiteFooter";
import { api } from "../lib/api";
import type { RoomSummary } from "../types/room";
import { mapRoomFromApi } from "../types/room";

declare global {
  interface Window {
    kakao: any;         // 카카오맵 SDK가 타입스크립트용 타입 정의를 제공하지 않기 때문에 any 사용
  }
}

const RoomMap: React.FC = () => {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);       // 지도 인스턴스를 저장할 ref
  const clustererRef = useRef<any>(null);         // 클러스터러 인스턴스를 저장할 ref
  const location = useLocation();                 // 현재 경로 정보를 가져옵니다.
  const [rooms, setRooms] = useState<RoomSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!window.kakao || !window.kakao.maps) {
      setError("카카오맵 SDK를 불러오지 못했습니다.");
      setIsLoading(false);
      return;
    }

    const fetchRoomsForCurrentLocation = (map: any) => {
      if (!map) return;
      const center = map.getCenter();
      const lat = center.getLat();
      const lng = center.getLng();
      fetchRoomsNearby(lat, lng);
    };

    // 지도 인스턴스가 이미 생성되었다면, 데이터만 새로 불러옵니다.
    if (mapInstanceRef.current) {
      fetchRoomsForCurrentLocation(mapInstanceRef.current);
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

            // 3. 현재 위치 기반으로 주변 방 데이터 요청
            fetchRoomsNearby(latitude, longitude);
          },
          () => {
            // 위치 정보 가져오기 실패 시 기본 위치(서울)로 설정
            setError("위치 정보를 가져올 수 없습니다. 기본 위치로 지도를 표시합니다.");
            const defaultPosition = new window.kakao.maps.LatLng(37.5665, 126.9780);
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
            // 기본 위치 주변 방 데이터 요청
            fetchRoomsNearby(37.5665, 126.9780);
          }
        );
      } else {
        setError("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        setIsLoading(false);
      }
    });

    const fetchRoomsNearby = async (lat: number, lng: number) => {
      setIsLoading(true);
      setError(null);
      try {
        // 4. API로 주변 방 데이터 가져오기 (API 명세에 따라 파라미터 조정 필요)
        const { data } = await api.get("/map/rooms/near", {
          params: { lat: lat, lng: lng, radiusKm: 2 }, // 예: 2km 반경
        });

        const roomList: RoomSummary[] = Array.isArray(data) ? data.map(mapRoomFromApi) : [];
        setRooms(roomList);

        // 5. 가져온 방 데이터로 마커들을 생성
        const markers = roomList.map((room) => {
          if (room.latitude && room.longitude) {
            const markerPosition = new window.kakao.maps.LatLng(room.latitude, room.longitude);
            return new window.kakao.maps.Marker({
              position: markerPosition,
              title: room.title,
            });
          }
          return null;
        }).filter((marker): marker is any => marker !== null);

        // 6. 클러스터러에 마커들을 추가
        clustererRef.current.clear(); // 기존 마커들을 모두 제거
        clustererRef.current.addMarkers(markers); // 새로운 마커들을 추가
      } catch (err) {
        setError("주변 방 정보를 불러오는 중 오류가 발생했습니다.");
      } finally {
        setIsLoading(false);
      }
    };
  }, [location]); // location이 변경될 때마다 useEffect를 다시 실행합니다.

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

  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <SiteHeader activePath="/rooms" />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          position: "relative",
          width: "100%",
          height: "calc(100vh - 65px)", // 전체 화면 높이에서 헤더 높이(약 65px)를 뺌
        }}
      >
        <div ref={mapRef} style={{ width: "100%", height: "100%" }} />
        {(isLoading || error) && (
          <Box position="absolute" top={0} left={0} right={0} p={2} zIndex={10}>
            {isLoading && <CircularProgress />}
            {error && <Alert severity="warning">{error}</Alert>}
          </Box>
        )}
      </Box>
    </Box>
  );
};

export default RoomMap;
