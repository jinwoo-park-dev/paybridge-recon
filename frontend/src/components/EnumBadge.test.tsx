import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { EnumBadge } from './EnumBadge.js';

describe('EnumBadge', () => {
  it('renders a human-readable label for enum values', () => {
    render(<EnumBadge kind="type" value="AMOUNT_MISMATCH" />);
    expect(screen.getByText('Amount Mismatch')).toBeInTheDocument();
  });

  it('renders provider labels in uppercase', () => {
    render(<EnumBadge kind="provider" value="stripe" />);
    expect(screen.getByText('STRIPE')).toBeInTheDocument();
  });
});
