import React from 'react';
import { providerLabel, titleCaseEnum } from '../utils/format.js';
export function EnumBadge({ kind, value }) {
    const normalized = value ? value.toLowerCase() : 'unknown';
    const className = `badge badge-${kind}-${normalized}`;
    const label = kind === 'provider' ? providerLabel(value) : titleCaseEnum(value);
    return React.createElement("span", { className: className }, label);
}
