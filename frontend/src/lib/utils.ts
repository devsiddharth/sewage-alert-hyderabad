export function formatDate(iso: string | null | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" });
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

export function timeAgo(iso: string | null | undefined): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  const seconds = Math.floor((Date.now() - d.getTime()) / 1000);
  const units: [number, string][] = [
    [60, "second"],
    [60, "minute"],
    [24, "hour"],
    [7, "day"],
    [4.35, "week"],
    [12, "month"],
    [Number.POSITIVE_INFINITY, "year"],
  ];
  let value = seconds;
  let unit = "second";
  for (const [factor, name] of units) {
    if (value < factor) {
      unit = name;
      break;
    }
    value = Math.floor(value / factor);
  }
  return value <= 1 ? `1 ${unit} ago` : `${value} ${unit}s ago`;
}

export function initials(name: string | undefined | null): string {
  if (!name) return "?";
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]!.toUpperCase())
    .join("");
}

export function toTitleCase(value: string): string {
  return value
    .toLowerCase()
    .split("_")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

/** Friendly label for a complaint ID like "#SA-1042" */
export function complaintCode(id: number): string {
  return `#SA-${String(id).padStart(4, "0")}`;
}

/**
 * Compress an image file to a resized JPEG File (max dimension px, quality 0..1).
 *
 * The result is a real binary File (never a base64 string) so it can be attached to
 * a FormData request and uploaded to the backend's object storage. Keeps the original
 * compression behaviour that used to produce base64 data-URLs, but loads the source
 * image via an object URL instead of FileReader/base64.
 */
export function fileToCompressedFile(
  file: File,
  maxDimension = 1280,
  quality = 0.72
): Promise<File> {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file);
    const img = new Image();
    img.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error("Unsupported image"));
    };
    img.onload = () => {
      URL.revokeObjectURL(objectUrl);
      const scale = Math.min(1, maxDimension / Math.max(img.width, img.height));
      const canvas = document.createElement("canvas");
      canvas.width = Math.round(img.width * scale);
      canvas.height = Math.round(img.height * scale);
      const ctx = canvas.getContext("2d");
      if (!ctx) return reject(new Error("Canvas not supported"));
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      canvas.toBlob(
        (blob) => {
          if (!blob) return reject(new Error("Could not compress image"));
          const name = file.name.replace(/\.[^.]+$/, "") || "photo";
          resolve(new File([blob], `${name}.jpg`, { type: "image/jpeg" }));
        },
        "image/jpeg",
        quality
      );
    };
    img.src = objectUrl;
  });
}
