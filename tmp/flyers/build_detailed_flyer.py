from pathlib import Path

from docx import Document
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


ROOT = Path(__file__).resolve().parents[2]
DOCX_OUT = ROOT / "output" / "flyers" / "biobuzz_code_changes_detailed.docx"
DOCX_OUT.parent.mkdir(parents=True, exist_ok=True)

BLACK = "000000"
GRAY = "5E6269"
LIGHT_GRAY = "D9D9D9"
PURPLE = "3E205F"
VIOLET = "7245C7"
CYAN = "25AFC0"
GREEN = "2F925C"
GOLD = "D89D18"
RED = "C9414B"
WHITE = "FFFFFF"
PALE_PURPLE = "F6F2FA"


def set_run(run, size, bold=False, color=BLACK):
    run.font.name = "Arial"
    run._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), "Arial")
    run._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), "Arial")
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.color.rgb = RGBColor.from_string(color)
    return run


def add_text(paragraph, text, size=9.5, bold=False, color=BLACK, align=None,
             before=0, after=0, line=1.04):
    if align is not None:
        paragraph.alignment = align
    paragraph.paragraph_format.space_before = Pt(before)
    paragraph.paragraph_format.space_after = Pt(after)
    paragraph.paragraph_format.line_spacing = line
    return set_run(paragraph.add_run(text), size, bold, color)


def set_cell_fill(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=80, start=100, bottom=80, end=100):
    tc_pr = cell._tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for edge, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{edge}"))
        if node is None:
            node = OxmlElement(f"w:{edge}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_borders(cell, color=LIGHT_GRAY, size=7, visible=True):
    tc_pr = cell._tc.get_or_add_tcPr()
    borders = tc_pr.first_child_found_in("w:tcBorders")
    if borders is None:
        borders = OxmlElement("w:tcBorders")
        tc_pr.append(borders)
    for edge in ("top", "left", "bottom", "right"):
        elem = borders.find(qn(f"w:{edge}"))
        if elem is None:
            elem = OxmlElement(f"w:{edge}")
            borders.append(elem)
        elem.set(qn("w:val"), "single" if visible else "nil")
        if visible:
            elem.set(qn("w:sz"), str(size))
            elem.set(qn("w:color"), color)


def set_width(cell, inches):
    tc_pr = cell._tc.get_or_add_tcPr()
    tc_w = tc_pr.find(qn("w:tcW"))
    if tc_w is None:
        tc_w = OxmlElement("w:tcW")
        tc_pr.append(tc_w)
    tc_w.set(qn("w:w"), str(int(inches * 1440)))
    tc_w.set(qn("w:type"), "dxa")


def configure(doc):
    section = doc.sections[0]
    section.page_width = Inches(8.5)
    section.page_height = Inches(11)
    section.top_margin = Inches(0.42)
    section.bottom_margin = Inches(0.38)
    section.left_margin = Inches(0.52)
    section.right_margin = Inches(0.52)

    normal = doc.styles["Normal"]
    normal.font.name = "Arial"
    normal._element.rPr.rFonts.set(qn("w:ascii"), "Arial")
    normal._element.rPr.rFonts.set(qn("w:hAnsi"), "Arial")
    normal.font.size = Pt(9.5)
    normal.font.color.rgb = RGBColor.from_string(BLACK)

    title = doc.styles["Title"]
    title.font.name = "Arial"
    title._element.rPr.rFonts.set(qn("w:ascii"), "Arial")
    title._element.rPr.rFonts.set(qn("w:hAnsi"), "Arial")
    title.font.size = Pt(23)
    title.font.bold = True
    title.font.color.rgb = RGBColor.from_string(BLACK)
    title.paragraph_format.space_after = Pt(3)
    title_ppr = title._element.get_or_add_pPr()
    title_border = title_ppr.find(qn("w:pBdr"))
    if title_border is not None:
        title_ppr.remove(title_border)

    footer = section.footer.paragraphs[0]
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_text(footer, "BIOBUZZ 2026-27  |  SOFTWARE OVERVIEW", 7.3, True, GRAY)


def add_title(doc):
    kicker = doc.add_paragraph()
    add_text(kicker, "A ONE PAGE GUIDE TO THE NEW ROBOT SOFTWARE", 8.3, True, GRAY, after=1)

    title = doc.add_paragraph(style="Title")
    title.alignment = WD_ALIGN_PARAGRAPH.LEFT
    title.add_run("How Our New Robot Software Works")
    ppr = title._p.get_or_add_pPr()
    border = ppr.find(qn("w:pBdr"))
    if border is not None:
        ppr.remove(border)

    subtitle = doc.add_paragraph()
    add_text(
        subtitle,
        "The driver can request a path from anywhere on the field. The camera is used to aim the turret, not to unlock robot movement.",
        11.2,
        False,
        GRAY,
        after=5,
        line=1.08,
    )

    intro = doc.add_paragraph()
    add_text(
        intro,
        "The TeleOp assist separates navigation from vision. Road Runner tells the planner where the robot is, A* builds a safe route to a tested shooting location, and Pure Pursuit drives it. If the selected AprilTag becomes visible, Limelight aims the turret at the same time.",
        9.8,
        False,
        BLACK,
        after=7,
        line=1.08,
    )


def add_flow(doc):
    heading = doc.add_paragraph()
    add_text(heading, "The decision loop", 12, True, BLACK, after=3)

    colors = (VIOLET, CYAN, GREEN, PURPLE, GOLD)
    labels = ("SELECT", "PLAN", "FOLLOW", "AIM", "HAND BACK")
    explanations = (
        "Choose color and field side",
        "Use field pose to build a route",
        "Drive there with Pure Pursuit",
        "Use the tag to turn the turret",
        "Stop the assist at the shooting spot",
    )
    table = doc.add_table(rows=2, cols=5)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    for i in range(5):
        table.columns[i].width = Inches(1.47)
        top = table.cell(0, i)
        bottom = table.cell(1, i)
        for cell in (top, bottom):
            set_width(cell, 1.47)
            set_cell_margins(cell, 65, 60, 65, 60)
            set_borders(cell, visible=False)
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        set_cell_fill(top, colors[i])
        add_text(top.paragraphs[0], str(i + 1), 12.5, True, WHITE, WD_ALIGN_PARAGRAPH.CENTER, after=1)
        line = top.add_paragraph()
        add_text(line, labels[i], 7.6, True, WHITE, WD_ALIGN_PARAGRAPH.CENTER)
        add_text(bottom.paragraphs[0], explanations[i], 7.8, False, GRAY, WD_ALIGN_PARAGRAPH.CENTER, line=1.0)


def add_numbered_section(cell, number, title, body, color):
    set_borders(cell, visible=False)
    set_cell_margins(cell, 90, 75, 90, 145)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.TOP

    head = cell.paragraphs[0]
    badge = head.add_run(number + "  ")
    set_run(badge, 11.2, True, color)
    set_run(head.add_run(title), 11.2, True, BLACK)
    head.paragraph_format.space_after = Pt(2)
    head.paragraph_format.keep_with_next = True

    p = cell.add_paragraph()
    add_text(p, body, 9.15, False, GRAY, line=1.06)


def add_systems(doc):
    heading = doc.add_paragraph()
    add_text(heading, "Six systems working together", 12, True, BLACK, before=5, after=2)

    items = (
        (
            "1",
            "Driver selection",
            "The driver chooses red or blue and audience or far side. Holding the left bumper requests assistance. Releasing it, moving a drive stick, or pressing the right bumper cancels immediately.",
            VIOLET,
        ),
        (
            "2",
            "Path planning",
            "Road Runner supplies the robot's field position. A* compares the tested shooting locations for the selected side and builds the shortest reachable route around the Hive. No AprilTag is required to start.",
            CYAN,
        ),
        (
            "3",
            "Pure Pursuit driving",
            "Pure Pursuit follows the route across the field and turns the robot toward the Hive at the destination. When the path finishes, the drivetrain stops assisting and returns control to the driver.",
            GREEN,
        ),
        (
            "4",
            "Limelight turret aiming",
            "Limelight filters for the selected Hive AprilTag. When that tag enters view, its horizontal angle error turns the turret toward zero. Losing the tag stops the turret but does not cancel the path.",
            PURPLE,
        ),
        (
            "5",
            "Independent systems",
            "Navigation and aiming update in the same TeleOp loop without waiting on each other. The robot can cross the field before the tag is visible, then begin aiming as soon as the camera has a useful view.",
            GOLD,
        ),
        (
            "6",
            "Scope and safety",
            "This assist controls the drivetrain and turret only. It does not fire the shooter, run the transfer, or enter a park state. Geometry and turret motion remain locked until their calibration flags are verified.",
            RED,
        ),
    )

    table = doc.add_table(rows=3, cols=2)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    for col in range(2):
        table.columns[col].width = Inches(3.68)
    for i, item in enumerate(items):
        row, col = divmod(i, 2)
        cell = table.cell(row, col)
        set_width(cell, 3.68)
        add_numbered_section(cell, *item)


def add_key_explanations(doc):
    heading = doc.add_paragraph()
    add_text(heading, "Two important ideas", 12, True, BLACK, before=4, after=2)

    table = doc.add_table(rows=1, cols=2)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    entries = (
        (
            "The camera is not a gate",
            "The path starts from localization even when the Hive is across the field or outside the camera view. Seeing the selected tag only changes whether the turret can aim at that moment.",
            VIOLET,
        ),
        (
            "One assist cycle",
            "Select the target side, hold the bumper, and follow the generated route. Limelight aims whenever the tag appears. At the shooting position, the path ends with no automatic firing and no parking step.",
            GREEN,
        ),
    )
    for i, (title, body, color) in enumerate(entries):
        cell = table.cell(0, i)
        table.columns[i].width = Inches(3.68)
        set_width(cell, 3.68)
        set_borders(cell, visible=False)
        set_cell_margins(cell, 75, 75, 75, 145)
        p = cell.paragraphs[0]
        add_text(p, title, 10.1, True, color, after=2)
        p2 = cell.add_paragraph()
        add_text(p2, body, 8.85, False, GRAY, line=1.04)


def add_calibration(doc):
    heading = doc.add_paragraph()
    add_text(heading, "What must be checked before enabling it", 12, True, BLACK, before=4, after=3)

    items = (
        "Field geometry and shooting coordinates",
        "Road Runner start pose and localization",
        "Turret direction and mechanical limits",
        "Turret gain power and aim tolerance",
        "Selected AprilTag IDs and pipeline",
        "Safe shooting range measurements",
    )
    table = doc.add_table(rows=2, cols=3)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    for col in range(3):
        table.columns[col].width = Inches(2.46)
    for i, item in enumerate(items):
        row, col = divmod(i, 3)
        cell = table.cell(row, col)
        set_width(cell, 2.46)
        set_borders(cell, LIGHT_GRAY, 7, True)
        set_cell_margins(cell, 75, 90, 75, 90)
        if row == 1:
            set_cell_fill(cell, PALE_PURPLE)
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        p = cell.paragraphs[0]
        mark = p.add_run("CHECK  ")
        set_run(mark, 7.8, True, VIOLET)
        set_run(p.add_run(item), 8.45, False, GRAY)
        p.paragraph_format.line_spacing = 1.02

    closing = doc.add_paragraph()
    add_text(
        closing,
        "Until those values are measured on the real robot, drivetrain assistance and automatic turret motion stay locked. This TeleOp ends at the selected shooting location and gives control back to the driver - it has no park state.",
        8.8,
        False,
        BLACK,
        before=4,
        line=1.05,
    )


def build():
    doc = Document()
    configure(doc)
    add_title(doc)
    add_flow(doc)
    add_systems(doc)
    add_key_explanations(doc)
    add_calibration(doc)
    doc.save(DOCX_OUT)
    print(DOCX_OUT)


if __name__ == "__main__":
    build()
