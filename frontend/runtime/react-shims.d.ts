declare module 'react' {
  const React: any;
  export default React;
  export const Fragment: any;
  export function useEffect(effect: () => void | (() => void), deps?: unknown[]): void;
  export function useMemo<T>(factory: () => T, deps: unknown[]): T;
  export function useState<T>(initialState: T | (() => T)): [T, (value: T | ((previous: T) => T)) => void];
}

declare module 'react-dom/client' {
  export function createRoot(container: Element | DocumentFragment): {
    render(node: any): void;
  };
}
