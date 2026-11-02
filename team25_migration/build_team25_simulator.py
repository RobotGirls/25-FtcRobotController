#!/usr/bin/env python3
"""Copy Team 25 FieldViewer simulator into an EXISTING checked-out issue-39 repository.
No Git commits or pushes are performed. Requires internet on the user's computer.
"""
import argparse
import pathlib
import shutil
import tempfile
import urllib.request
import zipfile

SOURCE_ZIP = 'https://github.com/RobotGirls/25-FtcRobotController/archive/refs/heads/issue-38-purePursuitTesting.zip'
TARGET_BRANCH = 'issue-39-purePursuitStimulatorTesting'
RESOURCE = pathlib.Path('MeepMeepTesting/src/main/resources/biobuzz-field.png')
THEME = '''\n/* Team 25 purple simulator theme — overlay on original FieldViewer styles */\n:root {\n  --bg: #0f0a19;\n  --panel: #191029;\n  --panel-2: #24173a;\n  --border: rgba(194, 155, 255, 0.15);\n  --border-strong: rgba(194, 155, 255, 0.33);\n  --text: #f3edff;\n  --muted: #c2b1d8;\n  --faint: #806c9f;\n  --accent: #b77aff;\n  --accent-soft: rgba(183, 122, 255, 0.17);\n  --blue: #c19bff;\n  --sketch: #e1a4ff;\n}\nhtml, body { background: var(--bg); }\n.topbar { background: linear-gradient(110deg, #23123b, #130c21 80%); }\n.logo { background: radial-gradient(circle at 30% 30%, #d7b5ff, #6e30b0 72%); box-shadow: 0 0 20px rgba(166, 94, 255, .5); }\n.logo span { border-color: #f9efff; }\n.segmented button.active { box-shadow: inset 0 0 0 1px rgba(183,122,255,.48); }\nbutton:focus-visible, input:focus-visible { outline-color: #c19bff; }\n'''

def transform_text(s, suffix):
    # 5218 was also used as the backend TCP port. Use 2525 (instead of privileged port 25).
    s = s.replace('localhost:5218', 'localhost:2525')
    s = s.replace('const PORT = 5218', 'const PORT = 2525')
    s = s.replace('DEFAULT_PORT = 25;', 'DEFAULT_PORT = 2525;')
    s = s.replace('DEFAULT_PORT = 5218', 'DEFAULT_PORT = 2525')
    s = s.replace('PORT = 5218', 'PORT = 2525')
    s = s.replace('getProperty("fieldviewer.port", "5218")', 'getProperty("fieldviewer.port", "2525")')
    s = s.replace('getProperty("port", "5218")', 'getProperty("port", "2525")')
    s = s.replace('5218', '25')
    if suffix == '.css' and '/* Team 25 purple simulator theme' not in s:
        s += THEME
    return s

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('target', help='Path to local RobotGirls/25-FtcRobotController checkout')
    args = parser.parse_args()
    target = pathlib.Path(args.target).expanduser().resolve()
    if not (target / '.git').exists() or not (target / 'settings.gradle').is_file():
        raise SystemExit('ERROR: Target must be your local Git checkout containing .git and settings.gradle.')
    import subprocess
    branch = subprocess.check_output(['git','-C',str(target),'branch','--show-current'], text=True).strip()
    if branch != TARGET_BRANCH:
        raise SystemExit(f'ERROR: Checkout branch {TARGET_BRANCH} first. Current branch: {branch!r}')
    status = subprocess.check_output(['git','-C',str(target),'status','--porcelain'], text=True)
    if status.strip():
        raise SystemExit('ERROR: Your target checkout has uncommitted changes. Commit/stash those first so nothing is overwritten.')
    if (target / 'FieldViewer').exists() or (target / 'PurePursuit').exists():
        raise SystemExit('ERROR: FieldViewer or PurePursuit already exists here. Aborting rather than overwriting.')
    print('Downloading issue-38 simulator source from GitHub...')
    with tempfile.TemporaryDirectory() as td:
        temp = pathlib.Path(td)
        archive = temp / 'source.zip'
        urllib.request.urlretrieve(SOURCE_ZIP, archive)
        with zipfile.ZipFile(archive) as z:
            z.extractall(temp / 'source')
        roots = [p for p in (temp / 'source').iterdir() if p.is_dir()]
        if len(roots) != 1:
            raise SystemExit('ERROR: Unexpected GitHub ZIP structure.')
        src = roots[0]
        if not (src / 'FieldViewer').is_dir() or not (src / 'PurePursuit').is_dir():
            raise SystemExit('ERROR: Source archive is missing FieldViewer or PurePursuit.')
        for module in ['FieldViewer','PurePursuit']:
            shutil.copytree(src / module, target / module)
        resource_target = target / RESOURCE
        if not resource_target.is_file():
            resource_target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src / RESOURCE, resource_target)
        # Only change files in the simulator modules. Replace Java package directory name too.
        for module in ['FieldViewer','PurePursuit']:
            for path in list((target / module).rglob('*')):
                if not path.is_file():
                    continue
                if path.suffix.lower() in {'.java','.md','.gradle','.ts','.css','.html','.js','.json','.mjs','.xml','.txt'}:
                    try:
                        original = path.read_text('utf-8')
                    except UnicodeDecodeError:
                        continue
                    revised = transform_text(original, path.suffix.lower())
                    if revised != original:
                        path.write_text(revised, encoding='utf-8')
        old_pkg = target / 'FieldViewer/src/main/java/org/team5218'
        new_pkg = target / 'FieldViewer/src/main/java/org/team25'
        if old_pkg.exists():
            if new_pkg.exists():
                raise SystemExit('ERROR: Unexpected package directory conflict.')
            old_pkg.rename(new_pkg)
        settings = target / 'settings.gradle'
        content = settings.read_text('utf-8')
        additions = []
        for module in ['PurePursuit','FieldViewer']:
            if f"include ':{module}'" not in content and f'include(\":{module}\")' not in content:
                additions.append(f"include ':{module}'")
        if additions:
            settings.write_text(content.rstrip() + '\n' + '\n'.join(additions) + '\n', encoding='utf-8')
        print('\nSUCCESS: Simulator files copied and purple theme applied.')
        print('Changes are ONLY local, uncommitted, and unpushed.')
        print('Run: git -C', str(target), 'status --short')
        print('Then follow the included README to build, review and commit.')

if __name__ == '__main__':
    main()
