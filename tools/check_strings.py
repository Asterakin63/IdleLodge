"""Sanity-check a rethemed Android strings file against its pre-retheme original.

Checks: well-formed XML, identical key set and element kinds, identical format
placeholders per string, identical plural quantities, unescaped apostrophes,
leftover upstream branding. Prints a summary and exits non-zero on errors.

Usage: python -I check_strings.py <original.xml> <rethemed.xml>
"""
import re
import sys
import xml.etree.ElementTree as ET

PLACEHOLDER = re.compile(r'%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[sdfxXc%]')
BRANDING = re.compile(r'Idle Fantasy|IdleFantasy|tristinbaker', re.I)


def load(path):
    entries = {}
    for el in ET.parse(path).getroot():
        name = el.get('name')
        if el.tag == 'string':
            entries[name] = ('string', el.get('translatable'), {'': ''.join(el.itertext())})
        elif el.tag == 'plurals':
            entries[name] = ('plurals', el.get('translatable'),
                             {i.get('quantity'): ''.join(i.itertext()) for i in el})
        elif el.tag == 'string-array':
            entries[name] = ('string-array', el.get('translatable'),
                             {str(n): ''.join(i.itertext()) for n, i in enumerate(el)})
    return entries


def bad_apostrophe(text):
    if len(text) >= 2 and text.startswith('"') and text.endswith('"'):
        return False
    return re.search(r"(?<!\\)'", text) is not None


def main():
    orig_path, new_path = sys.argv[1], sys.argv[2]
    errors, unchanged = [], 0
    try:
        new = load(new_path)
    except ET.ParseError as e:
        print(f'XML PARSE ERROR in {new_path}: {e}')
        sys.exit(1)
    orig = load(orig_path)

    for key in sorted(set(orig) - set(new)):
        errors.append(f'missing key: {key}')
    for key in sorted(set(new) - set(orig)):
        errors.append(f'extra key: {key}')

    for key in sorted(set(orig) & set(new)):
        okind, otr, ovals = orig[key]
        nkind, ntr, nvals = new[key]
        if okind != nkind or otr != ntr:
            errors.append(f'{key}: element kind/translatable changed')
            continue
        if set(ovals) != set(nvals):
            errors.append(f'{key}: plural quantities / array size changed')
            continue
        for q, oval in ovals.items():
            nval = nvals[q]
            if sorted(PLACEHOLDER.findall(oval)) != sorted(PLACEHOLDER.findall(nval)):
                errors.append(f'{key}[{q}]: placeholders {PLACEHOLDER.findall(oval)} -> {PLACEHOLDER.findall(nval)}')
            if bad_apostrophe(nval):
                errors.append(f"{key}[{q}]: unescaped apostrophe (use \\')")
            if BRANDING.search(nval) and otr != 'false':
                errors.append(f'{key}[{q}]: upstream branding left')
            if nval == oval:
                unchanged += 1

    print(f'{new_path}: {len(new)} entries, {unchanged} values unchanged, {len(errors)} errors')
    for e in errors[:80]:
        print('  ' + e)
    sys.exit(1 if errors else 0)


if __name__ == '__main__':
    main()
