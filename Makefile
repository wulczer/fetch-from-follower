DOCUMENT = fetch-from-follower

EXTRA_CLEAN = $(wildcard *.toc *.aux *.log \
	*.out *.nav *.snm *.vrb *.bbl *.blg *.eps $(LISTINGS_TEX)) $(PYGMENTS_JAVA_TEX) $(PYGMENTS_PROPS_TEX) $(FLOWCHART_PDFS)

IMAGES = $(wildcard *.png)

FLOWCHART_MSC = $(wildcard *.msc)
FLOWCHART_PDFS = $(patsubst %.msc,%.pdf,$(FLOWCHART_MSC))

PYGMENTS_JAVA_TEX = pygments_java.tex
PYGMENTS_PROPS_TEX = pygments_properties.tex
PYGMENTIZE = pygmentize

LISTINGS_JAVA = $(wildcard *.java)
LISTINGS_PROPS = $(wildcard *.properties)
LISTINGS_TEX = $(patsubst %.java,%.tex,$(LISTINGS_JAVA)) $(patsubst %.properties,%.tex,$(LISTINGS_PROPS))

.SECONDARY: $(IMAGES) $(FLOWCHART_PDFS) $(LISTINGS_TEX)

all: $(DOCUMENT).pdf

clean:
	rm -f $(DOCUMENT).pdf $(EXTRA_CLEAN)

full_%.eps: %.msc
	mscgen -Teps -o $@ $<

%.pdf: %.eps
	epstopdf -o $@ $<

%.pdf: full_%.pdf
	pdfcrop $< $@

pygments_%.tex:
	echo | $(PYGMENTIZE) -l $* -f latex -O full | sed -n \
		'/makeatletter/,/makeatother/p' > $@

%.tex : %.java
	$(PYGMENTIZE) -l java -f latex -o $@ $<

%.tex : %.properties
	$(PYGMENTIZE) -l properties -f latex -o $@ $<

%.pdf: %.tex $(IMAGES) $(FLOWCHART_PDFS) $(LISTINGS_TEX) $(PYGMENTS_JAVA_TEX) $(PYGMENTS_PROPS_TEX)
	pdflatex $<
	pdflatex $<
