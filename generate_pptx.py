import sys
import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def create_presentation(output_path):
    prs = Presentation()
    # 16:9 Widescreen dimensions
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Professional Minimalist Palette
    COLOR_BG = RGBColor(248, 250, 252)         # Slate 50 (#F8FAFC)
    COLOR_DARK_BG = RGBColor(15, 23, 42)        # Slate 900 (#0F172A)
    COLOR_CARD = RGBColor(255, 255, 255)        # Pure White
    COLOR_CARD_BORDER = RGBColor(226, 232, 240) # Slate 200 (#E2E8F0)
    COLOR_PRIMARY = RGBColor(15, 23, 42)       # Deep Slate / Dark Text
    COLOR_MUTED = RGBColor(100, 116, 139)      # Slate 500 (#64748B)
    COLOR_ACCENT = RGBColor(37, 99, 235)       # Royal Blue (#2563EB)
    COLOR_ACCENT_BG = RGBColor(238, 242, 255)  # Indigo 50
    COLOR_EMERALD = RGBColor(16, 185, 129)     # Emerald 500
    COLOR_WHITE = RGBColor(255, 255, 255)

    def set_slide_background(slide, color):
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = color
        bg.line.fill.background()
        return bg

    def add_header(slide, kicker, title):
        tb_kicker = slide.shapes.add_textbox(Inches(0.8), Inches(0.42), Inches(11.7), Inches(0.32))
        tf_kicker = tb_kicker.text_frame
        tf_kicker.word_wrap = True
        tf_kicker.margin_left = tf_kicker.margin_top = tf_kicker.margin_right = tf_kicker.margin_bottom = 0
        p_k = tf_kicker.paragraphs[0]
        p_k.text = kicker.upper()
        p_k.font.size = Pt(10)
        p_k.font.bold = True
        p_k.font.color.rgb = COLOR_ACCENT
        p_k.font.name = "Segoe UI"

        tb_title = slide.shapes.add_textbox(Inches(0.8), Inches(0.72), Inches(11.7), Inches(0.65))
        tf_title = tb_title.text_frame
        tf_title.word_wrap = True
        tf_title.margin_left = tf_title.margin_top = tf_title.margin_right = tf_title.margin_bottom = 0
        p_t = tf_title.paragraphs[0]
        p_t.text = title
        p_t.font.size = Pt(21)
        p_t.font.bold = True
        p_t.font.color.rgb = COLOR_PRIMARY
        p_t.font.name = "Segoe UI"

    def add_card(slide, left, top, width, height, bg_color=COLOR_CARD, border_color=COLOR_CARD_BORDER):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        if border_color:
            card.line.color.rgb = border_color
            card.line.width = Pt(1)
        else:
            card.line.fill.background()
        return card

    # =========================================================================
    # SLIDE 1: Title Slide
    # =========================================================================
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1, COLOR_DARK_BG)

    pill = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.0), Inches(1.1), Inches(4.2), Inches(0.4))
    pill.fill.solid()
    pill.fill.fore_color.rgb = RGBColor(30, 41, 59)
    pill.line.color.rgb = RGBColor(51, 65, 85)
    pill.text = "DSA-3 · ADVANCED ALGORITHMS REVIEW (MODULES 1–3)"
    for p in pill.text_frame.paragraphs:
        p.font.size = Pt(9.5)
        p.font.bold = True
        p.font.color.rgb = COLOR_WHITE
        p.font.name = "Segoe UI"
        p.alignment = PP_ALIGN.CENTER

    tb_title = s1.shapes.add_textbox(Inches(1.0), Inches(1.75), Inches(11.3), Inches(2.2))
    tf_t = tb_title.text_frame
    tf_t.word_wrap = True
    p1 = tf_t.paragraphs[0]
    p1.text = "Large-Scale Text Compression and\nRepeated Pattern Mining System"
    p1.font.size = Pt(36)
    p1.font.bold = True
    p1.font.color.rgb = COLOR_WHITE
    p1.font.name = "Segoe UI"
    p1.space_after = Pt(12)

    p2 = tf_t.add_paragraph()
    p2.text = "Project TextHack · Question-Bank Architecture & Algorithmic Indexing"
    p2.font.size = Pt(16)
    p2.font.color.rgb = RGBColor(148, 163, 184)
    p2.font.name = "Segoe UI"

    add_card(s1, Inches(1.0), Inches(4.4), Inches(11.333), Inches(2.0), RGBColor(30, 41, 59), RGBColor(51, 65, 85))
    tb_team = s1.shapes.add_textbox(Inches(1.3), Inches(4.6), Inches(10.7), Inches(1.6))
    tf_team = tb_team.text_frame
    tf_team.word_wrap = True
    pt = tf_team.paragraphs[0]
    pt.text = "PROJECT TEAM MEMBERS"
    pt.font.size = Pt(11)
    pt.font.bold = True
    pt.font.color.rgb = RGBColor(56, 189, 248)
    pt.space_after = Pt(8)

    members = [
        ("Anvith", "2510030191"),
        ("Sai Srinath", "2510030261"),
        ("Keerthana S", "2510030039"),
        ("Keerthana V", "2510030041")
    ]
    p_mem = tf_team.add_paragraph()
    p_mem.text = "   •   ".join([f"{name} ({reg})" for name, reg in members])
    p_mem.font.size = Pt(13)
    p_mem.font.color.rgb = COLOR_WHITE
    p_mem.font.name = "Segoe UI"
    p_mem.space_after = Pt(8)

    p_stack = tf_team.add_paragraph()
    p_stack.text = "Syllabus Coverage: Module 1 (System Framing), Module 2 (String Algorithms), Module 3 (Advanced DP)\nImplementation: Pure Java · Zero Third-Party Algorithm Libraries · Indian Corpora (Hindi, Telugu, English)"
    p_stack.font.size = Pt(11)
    p_stack.font.color.rgb = RGBColor(148, 163, 184)

    # =========================================================================
    # SLIDE 2: Problem Statement & Motivation
    # =========================================================================
    s2 = prs.slides.add_slide(blank_layout)
    set_slide_background(s2, COLOR_BG)
    add_header(s2, "Motivation & Problem Statement", "Why Advanced Algorithmic Text Analytics Matters")

    cards_data = [
        ("1. The Scale Problem", 
         "Corpora are far too large for naive O(n²) string algorithms.",
         "Modern text corpora (multilingual Wikipedia dumps, genomic sequences, server logs) contain millions of tokens. Quadratic comparisons fail to execute in acceptable time frames."),
        ("2. Massive Redundancy", 
         "Natural text repeats phrases and boilerplate constantly.",
         "Indian-language corpora repeat official terms, boilerplate, and idioms. Storing and scanning redundant substrings repeatedly wastes storage and compute cycles."),
        ("3. Beyond DSA-1 / DSA-2", 
         "Standard linear/tree structures cannot solve sub-quadratic mining.",
         "Standard BSTs, HashMaps, and naive graphs cannot perform sub-quadratic substring search, optimal sequence alignment, or capacity-constrained assignment.")
    ]

    left_m = Inches(0.8)
    c_w = Inches(3.64)
    gap = Inches(0.4)
    top_p = Inches(1.65)
    c_h = Inches(5.1)

    for i, (title, highlight, desc) in enumerate(cards_data):
        c_left = left_m + i * (c_w + gap)
        add_card(s2, c_left, top_p, c_w, c_h)
        tb = s2.shapes.add_textbox(c_left + Inches(0.3), top_p + Inches(0.35), c_w - Inches(0.6), c_h - Inches(0.7))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = COLOR_ACCENT
        p.space_after = Pt(10)

        p2 = tf.add_paragraph()
        p2.text = highlight
        p2.font.size = Pt(12.5)
        p2.font.bold = True
        p2.font.color.rgb = COLOR_PRIMARY
        p2.space_after = Pt(12)

        p3 = tf.add_paragraph()
        p3.text = desc
        p3.font.size = Pt(11.5)
        p3.font.color.rgb = COLOR_MUTED
        p3.line_spacing = 1.35

    # =========================================================================
    # SLIDE 3: Module 1 — System Framing & Query-to-Algorithm Mapping
    # =========================================================================
    s3 = prs.slides.add_slide(blank_layout)
    set_slide_background(s3, COLOR_BG)
    add_header(s3, "Module 1 · System Framing", "TextHack as the Advanced-Algorithm Question Bank")

    # Card 1: Question Bank Concept & Canon
    add_card(s3, Inches(0.8), Inches(1.65), Inches(5.6), Inches(5.1))
    tb_m1a = s3.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(5.0), Inches(4.5))
    tf_m1a = tb_m1a.text_frame
    tf_m1a.word_wrap = True
    p = tf_m1a.paragraphs[0]
    p.text = "THE ADVANCED CANON & CONSTRAINTS"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    p = tf_m1a.add_paragraph()
    p.text = "• Working Engine on Day One:\n  Users issue queries against an Indian-language Wikipedia corpus and observe concrete outputs before inspecting internals.\n\n• Textbook Canon Territory:\n  Mapped directly to CLRS Part VII, Kleinberg-Tardos Ch 6–13, and Erickson Ch 4–12.\n\n• The Core Engine Constraint:\n  No java.util collections or regex for algorithms. All data structures and algorithms are manually written from scratch.\n\n• Public API Design First:\n  Every query class is designed with asymptotic time/space bounds before implementation."
    p.font.size = Pt(11.5)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.35

    # Card 2: Query Class to Algorithm Family Mapping Table
    add_card(s3, Inches(6.8), Inches(1.65), Inches(5.7), Inches(5.1))
    tb_m1b = s3.shapes.add_textbox(Inches(7.1), Inches(1.95), Inches(5.1), Inches(4.5))
    tf_m1b = tb_m1b.text_frame
    tf_m1b.word_wrap = True
    p = tf_m1b.paragraphs[0]
    p.text = "QUERY CLASS → ALGORITHM FAMILY MAPPING"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    mapping_text = (
        "1. Exact Pattern Search:\n"
        "   → String Algorithms (KMP, Z-Function, Rabin-Karp)\n\n"
        "2. Multi-Pattern Keyword Scan:\n"
        "   → Automaton Matching (Aho-Corasick)\n\n"
        "3. Repeated Pattern Mining:\n"
        "   → Suffix Indexing (Suffix Array + Kasai LCP)\n\n"
        "4. Fuzzy Matching & Alignment:\n"
        "   → Dynamic Programming (Wagner-Fischer)\n\n"
        "5. Citation Flow & Similarity (Future Scope):\n"
        "   → Max-Flow & Suffix Factorisation\n\n"
        "6. Project Scheduling & Primality (Future Scope):\n"
        "   → NP-Hard Approximation & Randomized Hashing"
    )
    p = tf_m1b.add_paragraph()
    p.text = mapping_text
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.3

    # =========================================================================
    # SLIDE 4: System Architecture & Data Flow Pipeline
    # =========================================================================
    s4 = prs.slides.add_slide(blank_layout)
    set_slide_background(s4, COLOR_BG)
    add_header(s4, "System Architecture", "Pipeline Architecture & Execution Flow")

    arch_cards = [
        ("1. Corpus Ingestion", 
         "UTF-8 Indian Language Ingestion", 
         "• CorpusLoader.java reads UTF-8 text.\n• Indian script preservation (Devanagari, Telugu, Tamil).\n• Zero external parsing libraries.\n• Reports character count, line count, and byte volume.\n• Supplies normalized text to all engine modules."),
        ("2. Search Dispatch", 
         "Query Routing to Algorithm Family", 
         "• Exact Single Search:\n  - KMP (LPS Prefix Array)\n  - Z-Function ([L, R] Window)\n  - Rabin-Karp (Double Hash)\n• Multi-Pattern Search:\n  - Aho-Corasick (Trie + BFS)\n• Fuzzy Alignment:\n  - Wagner-Fischer 2D DP"),
        ("3. Suffix Mining", 
         "Suffix Array + Kasai LCP", 
         "• SuffixArray.java:\n  Practical O(n log² n) Prefix Doubling.\n• KasaiLCP.java:\n  O(n) Linear LCP Construction.\n• PatternMiner.java:\n  LCP Interval Stack to discover maximal repeated patterns and corpus occurrences.")
    ]

    for i, (title, subtitle, desc) in enumerate(arch_cards):
        c_left = left_m + i * (c_w + gap)
        add_card(s4, c_left, top_p, c_w, c_h)
        tb = s4.shapes.add_textbox(c_left + Inches(0.3), top_p + Inches(0.35), c_w - Inches(0.6), c_h - Inches(0.7))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(15)
        p.font.bold = True
        p.font.color.rgb = COLOR_ACCENT
        p.space_after = Pt(4)

        p_s = tf.add_paragraph()
        p_s.text = subtitle
        p_s.font.size = Pt(11)
        p_s.font.bold = True
        p_s.font.color.rgb = COLOR_PRIMARY
        p_s.space_after = Pt(12)

        p_d = tf.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(11.5)
        p_d.font.color.rgb = COLOR_MUTED
        p_d.line_spacing = 1.35

    # =========================================================================
    # SLIDE 5: Module 2 — Exact String Matching (KMP, Z, Rabin-Karp)
    # =========================================================================
    s5 = prs.slides.add_slide(blank_layout)
    set_slide_background(s5, COLOR_BG)
    add_header(s5, "Module 2 · String Algorithms", "Exact Pattern Matching: KMP, Z-Algorithm & Rabin-Karp")

    exact_cards = [
        ("Knuth-Morris-Pratt (KMP)", "O(n + m) Time · O(m) Space", 
         "• Problem: Naive matching takes O(nm). KMP prevents re-scanning text.\n• Failure Function: lps[i] stores the length of the longest proper prefix that is also a suffix.\n• Preprocessing Intuition: On mismatch, len falls back to lps[len - 1].\n• Search: Text pointer i never rewinds; pattern pointer j shifts via LPS."),
        ("Linear Z-Algorithm", "O(n + m) Time · O(n + m) Space", 
         "• Modern Alternative to KMP: Computes Z-array for string P + $ + T in linear time.\n• [L, R] Window Logic: Maintains the furthest matching prefix segment.\n• In-Box Lookups: If i <= R, resolves Z[i] = Z[i - L] in O(1) if within remaining window.\n• Expand Beyond R: Only new characters are compared."),
        ("Rabin-Karp Rolling Hash", "Average O(n + m) · O(1) Space", 
         "• Polynomial Rolling Hash: Slides window in O(1) by subtracting outgoing & adding incoming char.\n• Modular Discipline: Large 64-bit primes prevent overflow.\n• Double Hashing: Uses MOD1=10⁹+7 & MOD2=10⁹+9 (collision rate ~10⁻¹⁸).\n• Character Verification: Guarantees zero false positives.")
    ]

    for i, (title, badge, desc) in enumerate(exact_cards):
        c_left = left_m + i * (c_w + gap)
        add_card(s5, c_left, top_p, c_w, c_h)
        tb = s5.shapes.add_textbox(c_left + Inches(0.3), top_p + Inches(0.35), c_w - Inches(0.6), c_h - Inches(0.7))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p = tf.paragraphs[0]
        p.text = title
        p.font.size = Pt(14.5)
        p.font.bold = True
        p.font.color.rgb = COLOR_PRIMARY
        p.space_after = Pt(4)

        p_b = tf.add_paragraph()
        p_b.text = badge
        p_b.font.size = Pt(10)
        p_b.font.bold = True
        p_b.font.color.rgb = COLOR_ACCENT
        p_b.space_after = Pt(12)

        p_d = tf.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(11)
        p_d.font.color.rgb = COLOR_MUTED
        p_d.line_spacing = 1.35

    # =========================================================================
    # SLIDE 6: Module 2 — Multi-Pattern Matching (Aho-Corasick)
    # =========================================================================
    s6 = prs.slides.add_slide(blank_layout)
    set_slide_background(s6, COLOR_BG)
    add_header(s6, "Module 2 · String Algorithms", "Multi-Pattern Matching: Aho-Corasick Automaton")

    add_card(s6, Inches(0.8), Inches(1.65), Inches(6.6), Inches(5.1))
    tb_ac = s6.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(6.0), Inches(4.5))
    tf_ac = tb_ac.text_frame
    tf_ac.word_wrap = True
    p = tf_ac.paragraphs[0]
    p.text = "TRIE-WITH-FAILURE-LINKS CONSTRUCTION"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    p = tf_ac.add_paragraph()
    p.text = "• Motivation:\n  Scanning text of length n for k patterns with single-pattern matchers requires O(k · n). Aho-Corasick solves this in a single pass of O(n + ∑m + z).\n\n• Trie Construction:\n  All patterns are inserted into a shared prefix tree.\n\n• BFS Failure Link Construction:\n  Failure links connect state v to the longest proper suffix state existing in the trie. Computed via BFS queue traversal so shallower states are resolved first.\n\n• Output Dictionary Suffix Links:\n  If a failure state represents a matched keyword, that keyword is added to the current state's output set."
    p.font.size = Pt(11.5)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.35

    add_card(s6, Inches(7.7), Inches(1.65), Inches(4.8), Inches(5.1), COLOR_DARK_BG, None)
    tb_aco = s6.shapes.add_textbox(Inches(8.0), Inches(1.95), Inches(4.2), Inches(4.5))
    tf_aco = tb_aco.text_frame
    tf_aco.word_wrap = True
    p = tf_aco.paragraphs[0]
    p.text = "VERIFIED DEMO OUTPUT"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = RGBColor(56, 189, 248)
    p.space_after = Pt(10)

    p = tf_aco.add_paragraph()
    p.text = "Text: \"ushers\"\nPatterns: [he, she, his, hers]\n\nResults:\n  • \"she\"   → Found at index 1\n  • \"he\"    → Found at index 2\n  • \"hers\"  → Found at index 2\n  • \"his\"   → 0 matches\n\nIndian Language Multi-Pattern:\nText: Hindi Wikipedia corpus\nPatterns: [भारत, भारतीय, संस्कृति]\n  → Matches all patterns concurrently in a single linear pass!"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_WHITE
    p.font.name = "Consolas"
    p.line_spacing = 1.3

    # =========================================================================
    # SLIDE 7: Module 2 — Suffix Array & Kasai LCP
    # =========================================================================
    s7 = prs.slides.add_slide(blank_layout)
    set_slide_background(s7, COLOR_BG)
    add_header(s7, "Module 2 · Suffix Structures", "Suffix Array, Kasai's LCP & Advanced Structures Preview")

    add_card(s7, Inches(0.8), Inches(1.65), Inches(5.6), Inches(5.1))
    tb_sa = s7.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(5.0), Inches(4.5))
    tf_sa = tb_sa.text_frame
    tf_sa.word_wrap = True
    p = tf_sa.paragraphs[0]
    p.text = "SUFFIX ARRAY (O(n log² n) DOUBLING)"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(6)

    p = tf_sa.add_paragraph()
    p.text = "• Definition:\n  Array of integers storing starting indices of all suffixes sorted in lexicographical order.\n\n• Prefix Doubling Technique:\n  Sorts substrings of length 2^k using rank pairs (rank[i], rank[i + 2^(k-1)]). Terminates in ⌈log₂ n⌉ sorting passes.\n\n• Conceptual SA-IS vs Doubling:\n  SA-IS achieves O(n) using induced sorting; prefix doubling achieves practical O(n log² n) with clean, explainable student code.\n\n• Banana Check: SA = [5, 3, 1, 0, 4, 2]"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.3

    add_card(s7, Inches(6.8), Inches(1.65), Inches(5.7), Inches(5.1))
    tb_kl = s7.shapes.add_textbox(Inches(7.1), Inches(1.95), Inches(5.1), Inches(4.5))
    tf_kl = tb_kl.text_frame
    tf_kl.word_wrap = True
    p = tf_kl.paragraphs[0]
    p.text = "KASAI LCP & SUFFIX AUTOMATA PREVIEW"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(6)

    p = tf_kl.add_paragraph()
    p.text = "• Kasai Linear O(n) Algorithm:\n  Uses the inverse SA (rank array). Traverses suffixes in text order. Suffix (i + 1) shares at least (h - 1) characters with its predecessor. Reusing (h - 1) guarantees <= 2n comparisons.\n\n• Banana LCP: [0, 1, 3, 0, 0, 2]\n\n• Suffix Automata & Trees Preview:\n  - Suffix Trees allow O(m) search but suffer 20x-40x memory pointer overhead.\n  - Suffix Automaton (DAWG) represents all substrings in O(n) states and transitions.\n  - Suffix Array + LCP provides the sweet spot: 4n bytes with equivalent query power!"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.3

    # =========================================================================
    # SLIDE 8: Module 2 — Repeated Pattern Mining
    # =========================================================================
    s8 = prs.slides.add_slide(blank_layout)
    set_slide_background(s8, COLOR_BG)
    add_header(s8, "Module 2 · Mining Pipeline", "Repeated Substring Mining via Suffix Array + LCP")

    add_card(s8, Inches(0.8), Inches(1.65), Inches(6.0), Inches(5.1))
    tb_pm = s8.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(5.4), Inches(4.5))
    tf_pm = tb_pm.text_frame
    tf_pm.word_wrap = True
    p = tf_pm.paragraphs[0]
    p.text = "LCP INTERVAL STACK ALGORITHM"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    p = tf_pm.add_paragraph()
    p.text = "• The Theoretical Insight:\n  In a sorted Suffix Array, suffixes sharing common prefixes are placed immediately adjacent. Repeated substrings are LCP intervals.\n\n• Linear Stack Processing:\n  Maintains a stack of (lcp, start_idx). When LCP drops, intervals with lcp >= minLength are popped.\n\n• Extracted Metadata:\n  - Pattern Substring: text[SA[start] ... SA[start] + LCP]\n  - Occurrences: (end_idx - start_idx + 1)\n  - All Starting Positions: {SA[start], ..., SA[end]}\n\n• Eliminates quadratic brute-force substring maps."
    p.font.size = Pt(11.5)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.35

    add_card(s8, Inches(7.1), Inches(1.65), Inches(5.4), Inches(5.1), COLOR_DARK_BG, None)
    tb_pmo = s8.shapes.add_textbox(Inches(7.4), Inches(1.95), Inches(4.8), Inches(4.5))
    tf_pmo = tb_pmo.text_frame
    tf_pmo.word_wrap = True
    p = tf_pmo.paragraphs[0]
    p.text = "LIVE MINING OUTPUT DEMO"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = RGBColor(56, 189, 248)
    p.space_after = Pt(10)

    p = tf_pmo.add_paragraph()
    p.text = "Text:\n\"banana is a banana. banana patterns...\"\nMin Pattern Length: 6\n\nRepeated Patterns Discovered:\nNo. | Pattern   | Length | Occurrences\n----+-----------+--------+------------\n1   | banana    |   6    |     3      \n2   |  banana   |   7    |     2      \n3   | banana    |   7    |     2      \n\nTop Repeated Pattern:\n• Pattern:     \"banana\"\n• Length:      6\n• Occurrences: 3\n• Positions:   [0, 12, 20]"
    p.font.size = Pt(10.5)
    p.font.color.rgb = COLOR_WHITE
    p.font.name = "Consolas"
    p.line_spacing = 1.25

    # =========================================================================
    # SLIDE 9: Module 3 — Advanced Dynamic Programming
    # =========================================================================
    s9 = prs.slides.add_slide(blank_layout)
    set_slide_background(s9, COLOR_BG)
    add_header(s9, "Module 3 · Advanced Dynamic Programming", "Wagner-Fischer, Alignment, Advanced DP Families & Limits")

    # Left: Implemented DP (Wagner-Fischer & Variants)
    add_card(s9, Inches(0.8), Inches(1.65), Inches(5.6), Inches(5.1))
    tb_m3a = s9.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(5.0), Inches(4.5))
    tf_m3a = tb_m3a.text_frame
    tf_m3a.word_wrap = True
    p = tf_m3a.paragraphs[0]
    p.text = "EDIT DISTANCE & SEQUENCE ALIGNMENT"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(8)

    p = tf_m3a.add_paragraph()
    p.text = "• Wagner-Fischer 2D DP (Implemented):\n  O(mn) table computing Levenshtein distance:\n  dp[i][j] = 1 + min(delete, insert, substitute).\n  Powers TextHack's fuzzy spelling match.\n\n• Edit Distance Variants:\n  - Levenshtein (ins, del, sub)\n  - Damerau-Levenshtein (transposition of adjacent chars)\n  - Weighted Edit Distance (variable operation costs)\n\n• Sequence Alignment for Genomes:\n  - Needleman-Wunsch: Global alignment\n  - Smith-Waterman: Local alignment\n  - Affine gap penalties (gap opening vs extension)"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.3

    # Right: Advanced DP Families & When DP is the Wrong Tool
    add_card(s9, Inches(6.8), Inches(1.65), Inches(5.7), Inches(5.1))
    tb_m3b = s9.shapes.add_textbox(Inches(7.1), Inches(1.95), Inches(5.1), Inches(4.5))
    tf_m3b = tb_m3b.text_frame
    tf_m3b.word_wrap = True
    p = tf_m3b.paragraphs[0]
    p.text = "ADVANCED DP FAMILIES & BOUNDARIES"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(8)

    p = tf_m3b.add_paragraph()
    p.text = "• Advanced Syllabus DP Families:\n  - Interval DP: Matrix-chain mult, Optimal BST (O(n³), O(n² log n)).\n  - Bitmask DP: TSP in O(2ⁿ · n²), Hamiltonian path.\n  - Tree DP: Subset sums, tree diameter, rerooting technique.\n  - SOS DP: Sum-over-subsets in O(n · 2ⁿ) for inclusion-exclusion.\n\n• When DP is the Wrong Tool:\n  1. No overlapping subproblems (e.g. Divide & Conquer suffices).\n  2. Greedy choice property holds (e.g. Huffman coding, MST).\n  3. Exponential state space without bitmask compression (NP-hard)."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.3

    # =========================================================================
    # SLIDE 10: Complexity Summary Table
    # =========================================================================
    s10 = prs.slides.add_slide(blank_layout)
    set_slide_background(s10, COLOR_BG)
    add_header(s10, "Syllabus Asymptotic Analysis", "Time & Space Complexity Across Modules 1–3")

    rows = 9
    cols = 5
    tbl_shape = s10.shapes.add_table(rows, cols, Inches(0.8), Inches(1.65), Inches(11.733), Inches(5.1))
    tbl = tbl_shape.table

    tbl.columns[0].width = Inches(2.2)
    tbl.columns[1].width = Inches(2.0)
    tbl.columns[2].width = Inches(2.4)
    tbl.columns[3].width = Inches(2.0)
    tbl.columns[4].width = Inches(3.133)

    table_data = [
        ["Algorithm", "Module", "Time Complexity", "Space Complexity", "Role in TextHack"],
        ["KMP", "Module 2 (Strings)", "O(n + m)", "O(m)", "Linear exact search without rewind"],
        ["Z-Algorithm", "Module 2 (Strings)", "O(n + m)", "O(n + m)", "Prefix matching via [L, R] window"],
        ["Rabin-Karp", "Module 2 & 6", "Average O(n + m)", "O(1)", "Double rolling hash + verification"],
        ["Aho-Corasick", "Module 2 (Strings)", "O(n + ∑m + z)", "O(∑m)", "Multi-pattern keyword automaton"],
        ["Suffix Array", "Module 2 (Strings)", "O(n log² n)", "O(n)", "Prefix doubling text index"],
        ["Kasai LCP", "Module 2 (Strings)", "O(n)", "O(n)", "Linear LCP build reusing (h - 1)"],
        ["Pattern Miner", "Module 2 (Strings)", "O(n) on SA+LCP", "O(repeats)", "Repeated pattern mining"],
        ["Edit Distance", "Module 3 (DP)", "O(m · n)", "O(m · n)", "Wagner-Fischer 2D DP fuzzy match"]
    ]

    for r_idx, row in enumerate(table_data):
        for c_idx, val in enumerate(row):
            cell = tbl.cell(r_idx, c_idx)
            cell.text = val
            cell.vertical_anchor = MSO_ANCHOR.MIDDLE
            p = cell.text_frame.paragraphs[0]
            p.font.name = "Segoe UI"
            if r_idx == 0:
                p.font.bold = True
                p.font.size = Pt(11)
                p.font.color.rgb = COLOR_WHITE
                cell.fill.solid()
                cell.fill.fore_color.rgb = COLOR_DARK_BG
            else:
                p.font.size = Pt(10.5)
                if c_idx == 0:
                    p.font.bold = True
                    p.font.color.rgb = COLOR_PRIMARY
                elif c_idx in (2, 3):
                    p.font.bold = True
                    p.font.color.rgb = COLOR_ACCENT
                else:
                    p.font.color.rgb = COLOR_MUTED
                cell.fill.solid()
                cell.fill.fore_color.rgb = COLOR_WHITE if r_idx % 2 == 1 else RGBColor(241, 245, 249)

    # =========================================================================
    # SLIDE 11: Live Terminal Demonstration Commands
    # =========================================================================
    s11 = prs.slides.add_slide(blank_layout)
    set_slide_background(s11, COLOR_BG)
    add_header(s11, "Live Demonstration", "Windows CMD Execution Sequence")

    add_card(s11, Inches(0.8), Inches(1.65), Inches(11.733), Inches(5.1), COLOR_DARK_BG, None)
    tb_cmd = s11.shapes.add_textbox(Inches(1.1), Inches(1.85), Inches(11.1), Inches(4.7))
    tf_cmd = tb_cmd.text_frame
    tf_cmd.word_wrap = True

    p = tf_cmd.paragraphs[0]
    p.text = "WINDOWS COMMAND PROMPT (CMD) — LIVE DEMO COMMANDS"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = RGBColor(56, 189, 248)
    p.space_after = Pt(8)

    commands_text = (
        "# 1. Compile all Java classes cleanly:\n"
        "compile.bat\n\n"
        "# 2. Run master test suite (64 automated unit tests):\n"
        "java -cp bin RunAllTests\n\n"
        "# 3. Demonstrate individual algorithms:\n"
        "java -cp bin KMP ABABABAB ABAB\n"
        "java -cp bin ZFunction ABABABAB ABAB\n"
        "java -cp bin RabinKarp ABABABAB ABAB\n"
        "java -cp bin AhoCorasick ushers he she his hers\n"
        "java -cp bin SuffixArray banana\n"
        "java -cp bin KasaiLCP banana\n"
        "java -cp bin PatternMiner \"banana is a banana. banana patterns are repeated.\" 6\n"
        "java -cp bin EditDistance kitten sitting\n"
        "java -cp bin CorpusLoader data/corpus.txt"
    )

    p = tf_cmd.add_paragraph()
    p.text = commands_text
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_WHITE
    p.font.name = "Consolas"
    p.line_spacing = 1.25

    # =========================================================================
    # SLIDE 12: Implementation Status & Future Roadmap
    # =========================================================================
    s12 = prs.slides.add_slide(blank_layout)
    set_slide_background(s12, COLOR_BG)
    add_header(s12, "Conclusion & Roadmap", "Phase 1 Implemented vs Phase 2 Future Scope")

    add_card(s12, Inches(0.8), Inches(1.65), Inches(5.6), Inches(5.1))
    tb_done = s12.shapes.add_textbox(Inches(1.1), Inches(1.95), Inches(5.0), Inches(4.5))
    tf_d = tb_done.text_frame
    tf_d.word_wrap = True
    p = tf_d.paragraphs[0]
    p.text = "PHASE 1: IMPLEMENTED (CURRENT)"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_EMERALD
    p.space_after = Pt(10)

    p = tf_d.add_paragraph()
    p.text = "✔ UTF-8 Indian-language corpus ingestion (Hindi, Telugu, English)\n✔ KMP exact matching with manual LPS failure function\n✔ Linear Z-function with [L, R] window\n✔ Rabin-Karp double rolling hash with character verification\n✔ Aho-Corasick multi-pattern Trie & BFS links\n✔ Practical Suffix Array prefix doubling O(n log² n)\n✔ Kasai linear O(n) LCP array via (h - 1) reuse\n✔ Repeated Pattern Mining via LCP interval stack\n✔ Wagner-Fischer 2D Dynamic Programming\n✔ 64/64 automated unit tests passing in CMD"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_PRIMARY
    p.line_spacing = 1.35

    add_card(s12, Inches(6.8), Inches(1.65), Inches(5.7), Inches(5.1))
    tb_future = s12.shapes.add_textbox(Inches(7.1), Inches(1.95), Inches(5.1), Inches(4.5))
    tf_f = tb_future.text_frame
    tf_f.word_wrap = True
    p = tf_f.paragraphs[0]
    p.text = "PHASE 2: FUTURE MODULES"
    p.font.size = Pt(13.5)
    p.font.bold = True
    p.font.color.rgb = COLOR_MUTED
    p.space_after = Pt(10)

    p = tf_f.add_paragraph()
    p.text = "⏳ LZ-style copy-token encoding and decoding\n⏳ DP-optimal parsing for minimum encoded size factorisation\n⏳ Actual file compression & decompression output\n⏳ Compression ratio benchmarking against gzip\n⏳ Cross-document similarity scoring\n⏳ Citation-flow analysis over academic corpora\n⏳ Extended randomized universal hash families\n⏳ Approximations for NP-hard scheduling"
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_MUTED
    p.line_spacing = 1.35

    # Save presentation
    prs.save(output_path)
    print(f"Presentation successfully updated and saved to: {output_path}")

if __name__ == "__main__":
    out_file = r"c:\Users\Lenovo\Downloads\DSA_Project\TextHack_DSA3_Review.pptx"
    create_presentation(out_file)
