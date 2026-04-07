/**
 * Stub cho specifier {@code #internal/nuxt/paths} (khai báo trong package.json {@code imports}).
 * Không import {@code nitropack/runtime} — tránh chuỗi lỗi {@code #nitro-internal-virtual/...} khi Node
 * load bundle SSR ngoài ngữ cảnh Nitro đầy đủ.
 * Giá trị mặc định phù hợp dev (base {@code /}).
 */
import { joinRelativeURL } from 'ufo'

export function baseURL() {
  return '/'
}

export function buildAssetsDir() {
  return '/_nuxt/'
}

export function buildAssetsURL(...path) {
  return joinRelativeURL(publicAssetsURL(), buildAssetsDir(), ...path)
}

export function publicAssetsURL(...path) {
  const publicBase = '/'
  return path.length ? joinRelativeURL(publicBase, ...path) : publicBase
}
