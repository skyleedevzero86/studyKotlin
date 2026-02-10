import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      { source: "/api/search", destination: "http://localhost:8090/api/search" },
      { source: "/api/ingest/:path*", destination: "http://localhost:8090/api/ingest/:path*" },
    ];
  },
};

export default nextConfig;
