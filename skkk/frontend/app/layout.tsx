import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "기술 블로그 시맨틱 검색",
  description: "의미 기반 블로그 포스팅 검색",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
