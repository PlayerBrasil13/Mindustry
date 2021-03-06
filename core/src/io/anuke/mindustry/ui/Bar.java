package io.anuke.mindustry.ui;

import io.anuke.arc.*;
import io.anuke.arc.function.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.arc.math.*;
import io.anuke.arc.math.geom.*;
import io.anuke.arc.scene.*;
import io.anuke.arc.scene.style.*;
import io.anuke.arc.util.pooling.*;
import io.anuke.mindustry.gen.*;

public class Bar extends Element{
    private static Rectangle scissor = new Rectangle();

    private FloatProvider fraction;
    private String name = "";
    private float value, lastValue, blink;
    private Color blinkColor = new Color();

    public Bar(String name, Color color, FloatProvider fraction){
        this.fraction = fraction;
        this.name = Core.bundle.get(name);
        this.blinkColor.set(color);
        lastValue = value = fraction.get();
        setColor(color);
    }

    public Bar(Supplier<String> name, Supplier<Color> color, FloatProvider fraction){
        this.fraction = fraction;
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            this.name = name.get();
            this.blinkColor.set(color.get());
            setColor(color.get());
        });
    }

    public Bar(){

    }

    public void set(Supplier<String> name, FloatProvider fraction, Color color){
        this.fraction = fraction;
        this.lastValue = fraction.get();
        this.blinkColor.set(color);
        setColor(color);
        update(() -> this.name = name.get());
    }

    public Bar blink(Color color){
        blinkColor.set(color);
        return this;
    }

    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get());
        if(!Mathf.isEqual(lastValue, computed)){
            blink = 1f;
            lastValue = computed;
        }

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").getWidth()){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.pushScissors(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").getWidth(), height);
                ScissorStack.popScissors();
            }
        }

        Draw.color();

        BitmapFont font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);

        font.setColor(Color.white);
        font.draw(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);

        Pools.free(lay);
    }
}
