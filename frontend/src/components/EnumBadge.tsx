import React from 'react';
import { providerLabel, titleCaseEnum } from '../utils/format.js';

interface EnumBadgeProps {
  kind: 'status' | 'type' | 'provider';
  value: string | null | undefined;
}

export function EnumBadge({ kind, value }: EnumBadgeProps) {
  const normalized = value ? value.toLowerCase() : 'unknown';
  const className = `badge badge-${kind}-${normalized}`;
  const label = kind === 'provider' ? providerLabel(value) : titleCaseEnum(value);
  return <span className={className}>{label}</span>;
}
