package uk.me.desert_island.rer.rei_stuff;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.LabelWidget;
import me.shedaniel.rei.gui.widget.RecipeBaseWidget;
import me.shedaniel.rei.gui.widget.SlotWidget;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import uk.me.desert_island.rer.WorldGenState;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;




public class WorldGenCategory implements RecipeCategory<WorldGenDisplay> {
    private static final Identifier DISPLAY_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/display.png");
    
    public static final Identifier CATEGORY_ID = new Identifier("roughlyenoughresources", "worldgen_category");
    @Override
    public Identifier getIdentifier() {
        return CATEGORY_ID;
    }
    
    @Override
    public Renderer getIcon() {
        return Renderer.fromItemStack(new ItemStack(Blocks.GRASS_BLOCK));
    }

    @Override
    public String getCategoryName() {
        return I18n.translate("rer.worldgen.category");
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<WorldGenDisplay> recipeDisplaySupplier, Rectangle bounds) {
        final WorldGenDisplay recipeDisplay = recipeDisplaySupplier.get();
        WorldGenRecipe recipe = recipeDisplay.recipe;
        Block block = recipe.output_block;

        Point startPoint = new Point(bounds.getMinX() + 2, bounds.getMinY() + 3);
        
        List<Widget> widgets = new LinkedList<>();
        LeftLabelWidget y_widget = new LeftLabelWidget(startPoint.x, startPoint.y + 3, "");
        LeftLabelWidget pct_widget = new LeftLabelWidget(startPoint.x, startPoint.y + 13, "");
        widgets.add(new RecipeBaseWidget(bounds) {
            
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                DimensionType dim = this.minecraft.player.dimension;
                WorldGenState wgstate = WorldGenState.byDimension(dim);
        
                int graph_height = 60;
                double max_portion = wgstate.get_max_portion(block);

                super.render(mouseX, mouseY, delta);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiLighting.disable();
                MinecraftClient.getInstance().getTextureManager().bindTexture(DISPLAY_TEXTURE);
                //blit(startPoint.x, startPoint.y, 0, 60, 103, 59);
                
                int mouse_height = mouseX - startPoint.x;
                if (mouse_height < 0 || mouse_height > 128) {
                    y_widget.text = "";
                    pct_widget.text = "";
                } else {
                    y_widget.text = String.format("%d", mouse_height);
                    pct_widget.text = String.format("%f%%", wgstate.get_portion_at_height(block, mouse_height)*100);
                }


                for (int height=0; height<128; height++) {
                    double portion = wgstate.get_portion_at_height(block, height);
                    double rel_portion;
                    if (max_portion == 0) {
                        rel_portion = 0;
                    } else {
                        rel_portion = portion / max_portion;
                    }
                    
                    fill(/*startx*/ startPoint.x + height,
                    /*starty*/ startPoint.y + (int)(graph_height * (1-rel_portion)),
                    /*endx  */ startPoint.x + height + 1,
                    /*endy  */ startPoint.y + graph_height,
                    /*color */ 0xff000000);
                    
                    fill(/*startx*/ startPoint.x + height,
                    /*starty*/ startPoint.y + (int)(graph_height * (1-portion)),
                    /*endx  */ startPoint.x + height + 1,
                    /*endy  */ startPoint.y + graph_height,
                    /*color */ 0xff00ff00);
                    
                    //System.out.printf("%s at %d: %g\n", block, height, portion);
                }
                
            }
        });
        widgets.add(new SlotWidget(
            (bounds.getMaxX() - (16+4)), bounds.getMinY()+4,
            Renderer.fromItemStack(recipe.output_stack), false, true
        ));
        widgets.add(new LabelWidget((int)bounds.getCenterX(), (int)bounds.getMaxY()-(3+8+2), Registry.BLOCK.getId(block).toString()));
        widgets.add(pct_widget);
        widgets.add(y_widget);
        return widgets;
    }
}
