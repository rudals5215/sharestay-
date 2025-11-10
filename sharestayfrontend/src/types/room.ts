export interface RoomImage {
  imageId: number;
  roomId: number;
  imageUrl: string;
  isPrimary?: boolean;
}

export type RoomAvailabilityStatus = "AVAILABLE" | "UNAVAILABLE" | "PENDING";

export interface RoomSummary {
  roomId: number;
  hostId: number;
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
  options?: string[] | string;
  images?: RoomImage[];
}

export interface RoomPayload {
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  options?: string[];
  description?: string;
  latitude?: number;
  longitude?: number;
  availabilityStatus?: RoomAvailabilityStatus | number;
  lifeStylePreference?: string;
}
