const assetBaseUrl = (import.meta.env.VITE_BASE_URL ?? "").replace(/\/$/, "");

export const resolveRoomImageUrl = (value?: string | null) => {
  if (!value) return undefined;
  if (/^https?:\/\//i.test(value)) return value;
  if (!assetBaseUrl) return value;
  const trimmed = value.startsWith("/") ? value.slice(1) : value;
  return `${assetBaseUrl}/${trimmed}`;
};

export interface RoomImageResponse {
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
  hostId?: number | null;
  hostUserId?: number | null;
  preferredGender?: string | null;
  preferredAge?: string | null;
  totalMembers?: number | null;
  lifestyle?: string[] | string | null;
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
  availabilityStatus: number;
  description: string;
  latitude: number;
  longitude: number;
  preferredGender?: string | null;
  preferredAge?: string | null;
  totalMembers?: number | null;
  options?: string[];
  lifestyle?: string[];
}

export interface RoomApiResponse {
  id: number;
  hostId?: number | null;
  hostUserId?: number | null;
  preferredGender?: string | null;
  preferredAge?: string | null;
  totalMembers?: number | null;
  lifestyle?: string[] | null;
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  availabilityStatus: number;
  description: string;
  options?: string | string[] | null;
  images: RoomImageResponse[];
  imageUrls?: string[];
  shareLinkUrl?: string | null;
  shareLink?: { linkUrl?: string | null };
}

export interface RoomDetailApiResponse {
  id: number;
  hostId?: number | null;
  hostUserId?: number | null;
  preferredGender?: string | null;
  preferredAge?: string | null;
  totalMembers?: number | null;
  lifestyle?: string[] | null;
  title: string;
  rentPrice: number;
  address: string;
  type: string;
  availabilityStatus: number;
  description: string;
  options?: string[] | string | null;
  latitude: number;
  longitude: number;
  images?: RoomImageResponse[];
  imageUrls?: string[];
  shareLinkUrl?: string | null;
  shareLink?: { linkUrl?: string | null };
}

export interface ShareLinkResponse {
  linkUrl: string;
}

export const mapRoomFromApi = (
  room: RoomApiResponse | RoomDetailApiResponse,
): RoomSummary => {
  const normalizedImages: RoomImage[] =
    room.images?.map((image) => ({
      id: image.id,
      imageId: image.id,
      roomId: room.id,
      imageUrl: resolveRoomImageUrl(image.imageUrl) ?? image.imageUrl ?? "",
    })) ??
    room.imageUrls?.map((url, index) => ({
      id: index,
      imageId: index,
      roomId: room.id,
      imageUrl: resolveRoomImageUrl(url) ?? url ?? "",
    })) ??
    [];

  return {
    roomId: room.id,
    id: room.id,
    hostId: "hostId" in room ? room.hostId : undefined,
    hostUserId: "hostUserId" in room ? room.hostUserId : undefined,
    preferredGender: "preferredGender" in room ? room.preferredGender : undefined,
    preferredAge: "preferredAge" in room ? room.preferredAge : undefined,
    totalMembers: "totalMembers" in room ? room.totalMembers : undefined,
    lifestyle: "lifestyle" in room ? room.lifestyle : undefined,
    title: room.title,
    rentPrice: room.rentPrice,
    address: room.address,
    type: room.type,
    availabilityStatus: room.availabilityStatus,
    description: room.description,
    options: "options" in room ? room.options : undefined,
    images: normalizedImages,
    shareLinkUrl: room.shareLinkUrl ?? room.shareLink?.linkUrl ?? undefined,
  };
};
