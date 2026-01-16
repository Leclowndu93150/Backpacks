package com.spydnel.backpacks.models;// Made with Blockbench 4.12.3

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackpackModel<T extends LivingEntity> extends AgeableListModel<T> {
	private final ModelPart base;
	private final ModelPart lid;

	public BackpackModel(ModelPart root) {
		this.base = root.getChild("base");
		this.lid = this.base.getChild("lid");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(40, 0).addBox(-7.0F, -10.0F, -4.5F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
				.texOffs(0, 0).addBox(-7.0F, -9.0F, -1.0F, 8.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 10.0F, 2.5F));

		PartDefinition lid = base.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 15).addBox(-4.5F, -1.0F, -2.0F, 9.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(32, 18).addBox(-4.5F, 0.0F, -2.0F, 9.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -9.0F, 0.5F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}


	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.base, this.lid);
	}
}