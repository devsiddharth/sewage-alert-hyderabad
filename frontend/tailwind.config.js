/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // Brand system per design spec
        brand: {
          DEFAULT: "#0A2463",
          dark: "#091540",
          light: "#12328a",
        },
        accent: {
          DEFAULT: "#7692FF",
          soft: "#EEF2FF",
        },
        success: {
          DEFAULT: "#55D6BE",
          soft: "#E6FAF5",
        },
        canvas: "#F8FAFC",
        ink: "#111827",
        muted: "#6B7280",
        line: "#E5E7EB",
      },
      fontFamily: {
        sans: [
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "sans-serif",
        ],
      },
      borderRadius: {
        xl: "0.75rem",
        "2xl": "1rem",
        "3xl": "1.25rem",
      },
      boxShadow: {
        card: "0 1px 2px 0 rgb(16 24 40 / 0.04), 0 1px 3px 0 rgb(16 24 40 / 0.06)",
        lift: "0 8px 24px -8px rgb(10 36 99 / 0.16)",
      },
      transitionDuration: {
        200: "200ms",
      },
      keyframes: {
        "fade-in": {
          "0%": { opacity: "0", transform: "translateY(4px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        shimmer: {
          "100%": { transform: "translateX(100%)" },
        },
      },
      animation: {
        "fade-in": "fade-in 200ms ease-out both",
        "fade-in-slow": "fade-in 400ms ease-out both",
      },
    },
  },
  plugins: [],
};
