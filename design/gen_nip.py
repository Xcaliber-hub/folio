import math, json
# Nip geometry, single source of truth. Units: nip box 80 x 36.
W,H=80,36
def rr(x,y,w,h,r):
    return (f"M{x+r:g},{y:g} H{x+w-r:g} A{r:g},{r:g} 0 0 1 {x+w:g},{y+r:g} V{y+h-r:g} "
            f"A{r:g},{r:g} 0 0 1 {x+w-r:g},{y+h:g} H{x+r:g} A{r:g},{r:g} 0 0 1 {x:g},{y+h-r:g} "
            f"V{y+r:g} A{r:g},{r:g} 0 0 1 {x+r:g},{y:g} Z")
def rect(x,y,w,h): return f"M{x:g},{y:g} H{x+w:g} V{y+h:g} H{x:g} Z"
def circ(cx,cy,r): return f"M{cx-r:g},{cy:g} A{r:g},{r:g} 0 1 0 {cx+r:g},{cy:g} A{r:g},{r:g} 0 1 0 {cx-r:g},{cy:g} Z"
P={}
P['ROD_LEFT']=rr(1,16.5,6,11,2)+" "+rect(4,19,23,6)
P['ROD_RIGHT']=rect(53,19,23,6)+" "+rr(73,16.5,6,11,2)
P['PILL_SOLID']=rr(24,13,32,18,9)
P['PILL']=P['PILL_SOLID']+" "+circ(33,22,2.3)+" "+circ(40,22,2.3)+" "+circ(47,22,2.3)
P['IBEAM']=rr(34,0,12,3,1.5)+" "+rect(38.7,3,2.6,7.2)+" "+rr(36,10.2,8,2.6,1.3)
json.dump(P,open('nip_paths.json','w'),indent=1)

# M3E Cookie 12-sided style container (scalloped), viewport 108 centred r 33 (adaptive safe zone)
def cookie(cx,cy,R,n=9,amp=0.055,steps=360):
    pts=[]
    for i in range(steps):
        t=2*math.pi*i/steps
        r=R*(1-amp+amp*math.cos(n*t))
        pts.append((cx+r*math.cos(t-math.pi/2),cy+r*math.sin(t-math.pi/2)))
    return "M"+" L".join(f"{x:.2f},{y:.2f}" for x,y in pts)+" Z"
COOKIE=cookie(54,54,38)
# nip placement in 108 viewport: scale so nip width = 52
S=56/W; TX=54-W*S/2; TY=54-H*S/2+1.5
json.dump({'cookie':COOKIE,'scale':S,'tx':TX,'ty':TY},open('logo_layout.json','w'))

def svg(bg='#FFFFFF', size=1024, cookie_fill='#000000', nip_fill='#FFFFFF', with_cookie=True, full_bleed=False):
    s=size/108
    body=''
    if with_cookie: body+=f'<path d="{COOKIE}" fill="{cookie_fill}"/>'
    g=f'<g transform="translate({TX:.3f},{TY:.3f}) scale({S:.4f})" fill="{nip_fill}">'
    for k in ['ROD_LEFT','ROD_RIGHT','IBEAM']: g+=f'<path d="{P[k]}"/>'
    g+=f'<path d="{P["PILL"]}" fill-rule="evenodd"/></g>'
    bgr=f'<rect width="108" height="108" fill="{bg}"/>' if bg else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 108 108">{bgr}<g>{body}{g}</g></svg>'
open('folio_logo.svg','w').write(svg())
open('folio_logo_transparent.svg','w').write(svg(bg=None))
open('folio_logo_inverted.svg','w').write(svg(bg='#000000',cookie_fill='#FFFFFF',nip_fill='#000000'))
import cairosvg
cairosvg.svg2png(url='folio_logo.svg',write_to='folio_logo_1024.png')
cairosvg.svg2png(url='folio_logo_inverted.svg',write_to='folio_logo_inverted_1024.png')
# nip alone big, for comparison sheet
nsvg=f'<svg xmlns="http://www.w3.org/2000/svg" width="800" height="360" viewBox="0 0 80 36"><rect width="80" height="36" fill="#fff"/>'+''.join(f'<path d="{P[k]}"/>' for k in ['ROD_LEFT','ROD_RIGHT','IBEAM'])+f'<path d="{P["PILL"]}" fill-rule="evenodd"/></svg>'
open('nip.svg','w').write(nsvg); cairosvg.svg2png(url='nip.svg',write_to='nip.png')
