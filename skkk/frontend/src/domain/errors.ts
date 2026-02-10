export type AppError =
    | { kind: "NetworkError"; message: string }
    | { kind: "ApiError"; status: number; message: string }
    | { kind: "ValidationError"; message: string }
    | { kind: "Unknown"; message: string };

export function toMessage(e: AppError): string {
    switch (e.kind) {
        case "NetworkError":
        case "ValidationError":
        case "Unknown":
            return e.message;
        case "ApiError":
            return `${e.status}: ${e.message}`;
    }
}
