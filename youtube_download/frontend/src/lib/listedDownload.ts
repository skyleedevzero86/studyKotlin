export type ListedDownload = {
  id: number;
  videoId: string;
  title: string | null;
  status: string;
  fileName: string | null;
  sizeBytes: number | null;
  errorMessage: string | null;
  updatedAt: string;
  fileUrl: string | null;
  likeCount: number;
  dislikeCount: number;
  viewCount: number;
};
