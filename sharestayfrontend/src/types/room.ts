const assetBaseUrl = (import.meta.env.VITE_BASE_URL ?? "").replace(/\/$/, "");

export const resolveRoomImageUrl = (value?: string | null) => {
  if (!value) return undefined;
  if (/^https?:\/\//i.test(value)) return value;
  if (!assetBaseUrl) return value;
  const trimmed = value.startsWith("/") ? value.slice(1) : value;
  return `${assetBaseUrl}/${trimmed}`;
};

export interface RoomImageApiResponse {
  id: number;
  imageUrl: string;
}

export interface RoomImage {
  id?: number;
  imageId?: number;
  roomId?: number;
  imageUrl: string;
  isPrimary?: boolean;
}

export type RoomAvailabilityStatus = "AVAILABLE" | "UNAVAILABLE" | "PENDING";

export interface RoomSummary {
  roomId?: number;
  id?: number;
  hostId?: number;
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  latitude?: number;
  longitude?: number;
  availabilityStatus: RoomAvailabilityStatus | number;
  description?: string;
  safetyScore?: number;
  trustScore?: number;
  tags?: string[];
  isFavorite?: boolean;
  favoriteId?: number;
  options?: string[] | string | null;
  images?: RoomImage[];
  shareLinkUrl?: string;
}

export interface RoomRequestPayload {
  hostId: number;
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  latitude: number;
  longitude: number;
  availabilityStatus: number;
  description: string;
}

export interface RoomApiResponse {
  id: number;
  roomId?: number; // roomId 필드 추가
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  availabilityStatus: number;
  description?: string;
  images?: RoomImageApiResponse[];
  shareLinkUrl?: string | null;
  latitude?: number;
  longitude?: number;
}

export interface RoomDetailApiResponse extends RoomApiResponse {
  latitude?: number;
  longitude?: number;
  imageUrls?: string[];
  shareLinkUrl?: string | null;
}

export interface ShareLinkResponse {
  linkUrl: string;
}

export const mapRoomFromApi = (
  room: RoomApiResponse & {
    latitude?: number; longitude?: number;
  }
): RoomSummary => {
  const roomId = room.roomId ?? room.id; // roomId가 있으면 사용, 없으면 id 사용

  const normalizedImages: RoomImage[] =
    room.images?.map((image) => ({
      id: image.id,
      imageId: image.id,
      roomId: roomId,
      imageUrl: resolveRoomImageUrl(image.imageUrl) ?? image.imageUrl ?? "",
    })) ?? [];

  return {
    roomId: roomId,
    id: roomId,
    title: room.title,
    rentPrice: room.rentPrice,
    address: room.address,
    type: room.type,
    availabilityStatus: room.availabilityStatus,
    latitude: room.latitude,
    longitude: room.longitude,
    description: room.description,
    images: normalizedImages,
    shareLinkUrl: room.shareLinkUrl ?? undefined,
  };
};
