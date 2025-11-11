export interface RoomImage {
  imageId: number;
  roomId: number;
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
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  availabilityStatus: number;
  description?: string;
}

export const mapRoomFromApi = (room: RoomApiResponse): RoomSummary => ({
  roomId: room.id,
  id: room.id,
  title: room.title,
  rentPrice: room.rentPrice,
  address: room.address,
  type: room.type,
  availabilityStatus: room.availabilityStatus,
  description: room.description,
});
