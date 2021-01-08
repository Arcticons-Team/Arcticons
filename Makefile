COLOR = \#ffffff
ICONS = icons
CLEANED = cleaned
RASTER = app/src/main/res

SIZES = mdpi hdpi xhdpi xxhdpi xxxhdpi
SIZES.mdpi=48
SIZES.hdpi=72
SIZES.xhdpi=96
SIZES.xxhdpi=144
SIZES.xxxhdpi=192

SVG = $(wildcard $(ICONS)/*.svg)
NAMES = $(patsubst $(ICONS)/%.svg,%,$(SVG))

.SILENT:

all: raster xml

# raster needs to build for each size
raster: $(SIZES)

# --------------- XML stuff -------------
xml: $(RASTER)/xml/drawable.xml $(RASTER)/xml/appfilter.xml $(RASTER)/values/iconpack.xml

$(RASTER)/xml/drawable.xml: $(ICONS)
	echo Building drawable.xml
	echo '<?xml version="1.0" encoding="utf-8"?>' > $@
	echo '<resources>' >> $@
	echo '    <version>1</version>' >> $@
	echo $(NAMES) | tr " " "\n" | sed 's_.*_    <item drawable="&" \/>_' >> $@
	echo '</resources>' >> $@

$(RASTER)/xml/appfilter.xml: appfilter.txt
	echo Building appfilter.xml
	echo '<?xml version="1.0" encoding="utf-8"?>' > $@
	echo '<resources>' >> $@
	sort appfilter.txt | sed 's_\([^ ]*\) \([^ ]*\)_    <item component="ComponentInfo{\2}" drawable="\1" />_' >> $@
	echo '</resources>' >> $@

$(RASTER)/values/iconpack.xml: $(ICONS)
	echo Building iconpack.xml
	echo '<?xml version="1.0" encoding="utf-8"?>' > $@
	echo '<resources>' >> $@
	echo '    <string-array name="icon_pack" translatable="false">' >> $@
	echo $(NAMES) | tr " " "\n" | sed 's_.*_        <item>&<\/item>_' >> $@
	echo '    </string-array>' >> $@
	echo '</resources>' >> $@


# -------------------  Size targets  ------------------

# need a target for each size, this target will depend on all PNGs for that target 
# To generate these automatically we define a macro that generates a single one and loop over the size calling this macro
define CREATESIZE
$(1): $(foreach name,$(NAMES),$(RASTER)/drawable-$(1)/$(name).png) 

$(RASTER)/drawable-$(1)/%.png: $(CLEANED)/%.svg
	echo Rasterizing $$< to $$@
	mkdir -p $$(@D)
	inkscape --export-filename=$$@ --export-width=$(SIZES.$(1)) --export-height=$(SIZES.$(1)) $$< 2>/dev/null
endef
# now loop over the sizes calling the macro
$(foreach size,$(SIZES), $(eval $(call CREATESIZE,$(size))))


# -------------------  Cleaning the SVGs ------------------

# in case we need the target directly
cleanall: $(foreach name,$(NAMES),$(CLEANED)/$(name).svg)

# the second dependency ensured the directory exists
# this target both cleans the svg and sets the color of each stroke or fill to the correct one
$(CLEANED)/%.svg: $(ICONS)/%.svg 
	echo Cleaning $<
	mkdir -p $(CLEANED)
	scour --remove-descriptive-elements --enable-id-stripping --enable-viewboxing --enable-comment-stripping --nindent=4 -i $< -o $@ >/dev/null
	sed 's_\(fill\|stroke\):#[0-9A-Fa-f]\{3,6\};_\1:$(COLOR);_' $@ > $@.tmp
	mv $@.tmp $@


# -------------------  Other ------------------

clean: 
	rm -rf $(CLEANED)
	rm -rf $(foreach size,$(SIZES),$(RASTER)/drawable-$(size))
	rm -f $(RASTER)/xml/drawable.xml $(RASTER)/xml/appfilter.xml $(RASTER)/values/iconpack.xml
