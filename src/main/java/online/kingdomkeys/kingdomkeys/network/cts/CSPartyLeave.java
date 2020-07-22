package online.kingdomkeys.kingdomkeys.network.cts;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import online.kingdomkeys.kingdomkeys.capability.ExtendedWorldData;
import online.kingdomkeys.kingdomkeys.lib.Party;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncExtendedWorld;

public class CSPartyLeave {
	
	String name, username;
	UUID uuid, playerUUID;
	boolean priv;
	
	public CSPartyLeave() {}

	public CSPartyLeave(Party party, UUID playerUUID) {
		this.name = party.getName();
		this.uuid = party.getLeader().getUUID();
		this.username = party.getLeader().getUsername();
		this.priv = party.getPriv();
		this.playerUUID = playerUUID;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeInt(this.name.length());
		buffer.writeString(this.name);
		
		buffer.writeUniqueId(this.uuid);
		
		buffer.writeInt(this.username.length());
		buffer.writeString(this.username);
		
		buffer.writeBoolean(this.priv);
		
		buffer.writeUniqueId(this.playerUUID);
	}

	public static CSPartyLeave decode(PacketBuffer buffer) {
		CSPartyLeave msg = new CSPartyLeave();
		int length = buffer.readInt();
		msg.name = buffer.readString(length);
		
		msg.uuid = buffer.readUniqueId();
		
		length = buffer.readInt();
		msg.username = buffer.readString(length);
		
		msg.priv = buffer.readBoolean();
		
		msg.playerUUID = buffer.readUniqueId();
		return msg;
	}

	public static void handle(CSPartyLeave message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			ExtendedWorldData worldData = ExtendedWorldData.get(player.world);
			Party party = new Party(message.name, message.uuid, message.username, message.priv); 
			for(Party p : worldData.getParties()) {
				if(p.getName().equals(message.name)) {
					p.removeMember(message.playerUUID);
				}
			}
			PacketHandler.sendToAll(new SCSyncExtendedWorld(worldData), player.world);
		});
		ctx.get().setPacketHandled(true);
	}

}
