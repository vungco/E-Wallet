import type { Config } from 'tailwindcss'

export default {
  content: [
    './components/**/*.{js,vue,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './plugins/**/*.{js,ts}',
    './app.vue',
    './error.vue',
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        surface: {
          950: '#0a0f1a',
          900: '#0f172a',
          850: '#152238',
        },
        accent: {
          DEFAULT: '#2dd4bf',
          dim: '#14b8a6',
          glow: '#5eead4',
        },
      },
      boxShadow: {
        card: '0 0 0 1px rgba(45, 212, 191, 0.12), 0 25px 50px -12px rgba(0, 0, 0, 0.5)',
        glow: '0 0 40px -8px rgba(45, 212, 191, 0.35)',
      },
      backgroundImage: {
        'grid-pattern':
          'linear-gradient(rgba(45, 212, 191, 0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(45, 212, 191, 0.03) 1px, transparent 1px)',
      },
    },
  },
  plugins: [],
} satisfies Config
