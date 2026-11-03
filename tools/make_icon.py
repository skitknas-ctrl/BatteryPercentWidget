#!/usr/bin/env python3
"""Genererar den adaptiva appikonen: widgeten (batteriram, mätare, text) vid 67 %.
Samma tredelade rendering som BatteryWidget.renderWidget. Kör från projektroten:
    python3 tools/make_icon.py
"""
import os
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(HERE, "..", "app", "src", "main", "res")
FONT = "/usr/share/fonts/opentype/urw-base35/NimbusSansNarrow-Bold.otf"
PERCENT = 67
N = 432                      # xxxhdpi, 108 dp
ASPECT = 204 / 132           # 1x1-rutans proportioner på Pixel 7
BATTERY_H = 240              # batteriets höjd i ikonen (60 dp), inom säkra zonen
WL, WR, WT, WB = 0.111, 0.888, 0.108, 0.932
CT, CB, BAR_BOTTOM = 0.125, 0.915, 0.945
BAR_INSET, TEXT_INSET = 0.01, 0.10

frame = Image.open(os.path.join(HERE, "battery_frame_src.png")).convert("RGBA")
fw, fh = frame.size
font = ImageFont.truetype(FONT, 300)

def text_mask(t):
    tmp = Image.new("L", (1200, 600), 0)
    ImageDraw.Draw(tmp).text((50, 50), t, font=font, fill=255)
    return tmp.crop(tmp.getbbox()).rotate(90, expand=True)

def render(P, W, H):
    s = W / fw
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    capTopSrc, capBotSrc = round(fh * CT), round(fh * CB)
    mt, mb = capTopSrc * s, H - (fh - capBotSrc) * s
    winL, winR = W * WL, W * WR
    winT, winB = mt - (CT - WT) * fh * s, mb + (WB - CB) * fh * s
    # mätaren
    bar = Image.new("RGBA", (W, H), (0, 0, 0, 0)); d = ImageDraw.Draw(bar)
    bl, br = winL + W * BAR_INSET, winR - W * BAR_INSET
    bt, bb = winT, mb + (BAR_BOTTOM - CB) * fh * s
    ft = bb - (bb - bt) * P / 100
    d.rectangle([bl, bt, br, ft], fill=(0, 0, 0, 128))
    d.rectangle([bl, ft, br, bb], fill=(0, 255, 0, 128))
    out = Image.alpha_composite(out, bar)
    # ramen i tre delar
    fr = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    fr.paste(frame.crop((0, 0, fw, capTopSrc)).resize((W, round(mt)), Image.LANCZOS), (0, 0))
    fr.paste(frame.crop((0, capTopSrc, fw, capBotSrc)).resize((W, round(mb) - round(mt)), Image.LANCZOS), (0, round(mt)))
    fr.paste(frame.crop((0, capBotSrc, fw, fh)).resize((W, H - round(mb)), Image.LANCZOS), (0, round(mb)))
    out = Image.alpha_composite(out, fr)
    # texten
    mx, my = (winR - winL) * TEXT_INSET, (winB - winT) * TEXT_INSET
    tw, th = round(winR - winL - 2 * mx), round(winB - winT - 2 * my)
    m = text_mask(f"{P}%").resize((tw, th), Image.LANCZOS)
    tx = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    tx.paste(Image.new("RGBA", (tw, th), (255, 255, 255, 191)), (round(winL + mx), round(winT + my)), m)
    return Image.alpha_composite(out, tx)

# Bakgrund: mörkgrå. Förgrund: batteriet centrerat.
bg = Image.new("RGBA", (N, N), (40, 40, 40, 255))
bw = round(BATTERY_H / ASPECT)
battery = render(PERCENT, bw, BATTERY_H)
fg = Image.new("RGBA", (N, N), (0, 0, 0, 0))
fg.paste(battery, ((N - bw) // 2, (N - BATTERY_H) // 2), battery)

for name, size in [("mdpi", 108), ("hdpi", 162), ("xhdpi", 216), ("xxhdpi", 324), ("xxxhdpi", 432)]:
    d = os.path.join(RES, f"mipmap-{name}"); os.makedirs(d, exist_ok=True)
    for im, fn in [(bg, "ic_launcher_background"), (fg, "ic_launcher_foreground")]:
        (im if size == N else im.resize((size, size), Image.LANCZOS)).save(os.path.join(d, f"{fn}.png"))

# förhandsvisning med rund mask
prev = Image.alpha_composite(bg, fg)
mask = Image.new("L", (N, N), 0); ImageDraw.Draw(mask).ellipse([0, 0, N - 1, N - 1], fill=255)
out = Image.new("RGBA", (N, N), (90, 90, 90, 255)); out.paste(prev, (0, 0), mask)
out.save(os.path.join(HERE, "icon_preview.png"))
print("ikon klar")
