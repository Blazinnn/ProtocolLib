/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventObject;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.comphenix.protocol.async.AsyncMarker;

public class PacketEvent extends EventObject implements Cancellable {
	/**
	 * Automatically generated by Eclipse.
	 */
	private static final long serialVersionUID = -5360289379097430620L;

	private transient Player player;
	private PacketContainer packet;
	private boolean serverPacket;
	private boolean cancel;
	
	private AsyncMarker asyncMarker;
	private boolean asynchronous;
	
	/**
	 * Use the static constructors to create instances of this event.
	 * @param source - the event source.
	 */
	public PacketEvent(Object source) {
		super(source);
	}

	private PacketEvent(Object source, PacketContainer packet, Player player, boolean serverPacket) {
		super(source);
		this.packet = packet;
		this.player = player;
		this.serverPacket = serverPacket;
	}
	
	private PacketEvent(PacketEvent origial, AsyncMarker asyncMarker) {
		super(origial.source);
		this.packet = origial.packet;
		this.player = origial.player;
		this.cancel = origial.cancel;
		this.serverPacket = origial.serverPacket;
		this.asyncMarker = asyncMarker;
		this.asynchronous = true;
	}

	/**
	 * Creates an event representing a client packet transmission.
	 * @param source - the event source.
	 * @param packet - the packet.
	 * @param client - the client that sent the packet.
	 * @return The event.
	 */
	public static PacketEvent fromClient(Object source, PacketContainer packet, Player client) {
		return new PacketEvent(source, packet, client, false);
	}
	
	/**
	 * Creates an event representing a server packet transmission.
	 * @param source - the event source.
	 * @param packet - the packet.
	 * @param recipient - the client that will receieve the packet.
	 * @return The event.
	 */
	public static PacketEvent fromServer(Object source,  PacketContainer packet, Player recipient) {
		return new PacketEvent(source, packet, recipient, true);
	}
	
	/**
	 * Create an asynchronous packet event from a synchronous event and a async marker.
	 * @param event - the original synchronous event.
	 * @param marker - the asynchronous marker.
	 * @return The new packet event.
	 */
	public static PacketEvent fromSynchronous(PacketEvent event, AsyncMarker marker) {
		return new PacketEvent(event, marker);
	}
	
	/**
	 * Retrieves the packet that will be sent to the player.
	 * @return Packet to send to the player.
	 */
	public PacketContainer getPacket() {
		return packet;
	}

	/**
	 * Replace the packet that will be sent to the player.
	 * @param packet - the packet that will be sent instead.
	 */
	public void setPacket(PacketContainer packet) {
		this.packet = packet;
	}

	/**
	 * Retrieves the packet ID.
	 * @return The current packet ID.
	 */
	public int getPacketID() {
		return packet.getID();
	}
	
	/**
	 * Retrieves whether or not the packet should be cancelled.
	 * @return TRUE if it should be cancelled, FALSE otherwise.
	 */
	public boolean isCancelled() {
		return cancel;
	}

	/**
	 * Sets whether or not the packet should be cancelled.
	 * @param cancel - TRUE if it should be cancelled, FALSE otherwise.
	 */
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	/**
	 * Retrieves the player that has sent the packet or is recieving it.
	 * @return The player associated with this event.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Whether or not this packet was created by the server.
	 * <p>
	 * Most listeners can deduce this by noting which listener method was invoked.
	 * @return TRUE if the packet was created by the server, FALSE if it was created by a client.
	 */
	public boolean isServerPacket() {
		return serverPacket;
	}
	
	/**
	 * Retrieve the asynchronous marker.
	 * <p>
	 * If the packet is synchronous, this marker will be used to schedule an asynchronous event. In the following
	 * asynchronous event, the marker is used to correctly pass the packet around to the different threads.
	 * <p>
	 * Note that if there are no asynchronous events that can receive this packet, the marker is NULL.
	 * @return The current asynchronous marker, or NULL.
	 */
	public AsyncMarker getAsyncMarker() {
		return asyncMarker;
	}
	/**
	 * Set the asynchronous marker. 
	 * <p>
	 * If the marker is non-null at the end of an synchronous event processing, the packet will be scheduled
	 * to be processed asynchronously with the given settings.
	 * <p>
	 * Note that if there are no asynchronous events that can receive this packet, the marker should be NULL. 
	 * @param asyncMarker - the new asynchronous marker, or NULL.
	 * @throws IllegalStateException If the current event is asynchronous.
	 */
	public void setAsyncMarker(AsyncMarker asyncMarker) {
		if (isAsynchronous())
			throw new IllegalStateException("The marker is immutable for asynchronous events");
		this.asyncMarker = asyncMarker;
	}

	/**
	 * Determine if the packet event has been executed asynchronously or not.
	 * @return TRUE if this packet event is asynchronous, FALSE otherwise.
	 */
	public boolean isAsynchronous() {
		return asynchronous;
	}
	
	private void writeObject(ObjectOutputStream output) throws IOException {
	    // Default serialization 
		output.defaultWriteObject();

		// Write the name of the player (or NULL if it's not set)
		output.writeObject(player != null ? new SerializedOfflinePlayer(player) : null);
	}

	private void readObject(ObjectInputStream input) throws ClassNotFoundException, IOException {
	    // Default deserialization
		input.defaultReadObject();

		final SerializedOfflinePlayer offlinePlayer = (SerializedOfflinePlayer) input.readObject();
		
	    if (offlinePlayer != null) {
	    	// Better than nothing
	    	player = offlinePlayer.getPlayer();
	    }
	}
}
