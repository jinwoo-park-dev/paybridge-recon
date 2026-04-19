const zeroDecimalCurrencies = new Set(['JPY', 'KRW']);
export function formatInstant(value) {
    if (!value) {
        return '—';
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return value;
    }
    return new Intl.DateTimeFormat('en-US', {
        dateStyle: 'medium',
        timeStyle: 'medium',
        timeZone: 'UTC'
    }).format(parsed) + ' UTC';
}
export function formatMinorAmount(amountMinor, currency) {
    if (amountMinor === null || amountMinor === undefined) {
        return '—';
    }
    const safeCurrency = currency?.toUpperCase();
    if (!safeCurrency) {
        return String(amountMinor);
    }
    const fractionDigits = zeroDecimalCurrencies.has(safeCurrency) ? 0 : 2;
    try {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: safeCurrency,
            minimumFractionDigits: fractionDigits,
            maximumFractionDigits: fractionDigits
        }).format(amountMinor / Math.pow(10, fractionDigits));
    }
    catch {
        return `${amountMinor} ${safeCurrency}`;
    }
}
export function titleCaseEnum(value) {
    if (!value) {
        return '—';
    }
    return value
        .toLowerCase()
        .split('_')
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(' ');
}
export function providerLabel(value) {
    return value && value.trim().length > 0 ? value.toUpperCase() : 'Unknown';
}
