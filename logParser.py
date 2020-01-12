import re

mode = 'LARGE'

scans = {
    'LARGE': [],
    'SMALL': []
}

potential = {
    'LARGE': [],
    'SMALL': []
}

scan_regex = re.compile('\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} INFO  SwarmAgent:\d+ - (Agent \[r=\d+,g=\d+,b=\d+\]) scanned and discovered (\d+) coords')
potential_regex = re.compile('\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} INFO  SwarmAgent:\d+ - (Agent \[r=\d+,g=\d+,b=\d+\]) potentially discovering (\d+) coords')
size_regex = re.compile('\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} INFO  SwarmAgent:\d+ - (Agent \[r=\d+,g=\d+,b=\d+\]) switching to mode (SMALL|LARGE)')

with open('simulation.log') as f:
    for line in f:
        s = re.match(scan_regex, line)
        if s:
            agent, scanned = s.groups()
            scans[mode].append(int(scanned))
            continue
        p = re.match(potential_regex, line)
        if p:
            agent, pots = p.groups()
            potential[mode].append(int(pots))
            continue
        size = re.match(size_regex, line)
        if size:
            agent, sizing = size.groups()
            print(f'matched size to {sizing}')
            mode = sizing
print(scans)
print(potential)
