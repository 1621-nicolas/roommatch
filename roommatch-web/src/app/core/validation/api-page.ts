import { ApiResponse } from '../models/api-response';
import { PageResponse } from '../models/page-response';

/** TypeScript generics do not validate JSON received at runtime. Do not hide a broken contract as an empty list. */
export function requirePage<T>(response: ApiResponse<PageResponse<T>>): PageResponse<T> {
  const page = response?.data;
  if (response?.status !== 'success' || !page || !Array.isArray(page.content)
      || ![page.totalElements, page.totalPages, page.number, page.size].every(value => Number.isSafeInteger(value) && value >= 0)) {
    throw new Error('El servidor devolvió una lista con formato inesperado. Intenta recargar.');
  }
  return page;
}
