import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "영상 저장",
};

type RootLayoutProps = {
  children: ReactNode;
};

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
