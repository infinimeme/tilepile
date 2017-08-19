package com.infinimeme.tilepile.data;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

import com.infinimeme.tilepile.common.MainStation;
import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.Station;

public interface DataManagerRemote extends Remote {

    public static final String REMOTE_NAME = "TPDATA";

    public MainStation getMainStation(String name) throws RemoteException;

    public Mural getMural(String name) throws RemoteException;

    public Palette getPalette(String name) throws RemoteException;

    public States getStates(String name) throws RemoteException;

    public Station getStation(String name) throws RemoteException;

    public boolean containsMainStation(String name) throws RemoteException;

    public boolean containsMural(String name) throws RemoteException;

    public boolean containsPalette(String name) throws RemoteException;

    public boolean containsStates(String name) throws RemoteException;

    public boolean containsStation(String name) throws RemoteException;

    public Collection<MainStation> instancesOfMainStation() throws RemoteException;

    public Collection<Mural> instancesOfMural() throws RemoteException;

    public Collection<Palette> instancesOfPalette() throws RemoteException;

    public Collection<States> instancesOfStates() throws RemoteException;

    public Collection<Station> instancesOfStation() throws RemoteException;

    public Set<String> namesOfMainStation() throws RemoteException;

    public Set<String> namesOfMural() throws RemoteException;

    public Set<String> namesOfPalette() throws RemoteException;

    public Set<String> namesOfStates() throws RemoteException;

    public Set<String> namesOfStation() throws RemoteException;

    public void addMainStation(MainStation mainStation) throws RemoteException;

    public void addMural(Mural mural) throws RemoteException;

    public void addPalette(Palette palette) throws RemoteException;

    public void addStates(States state) throws RemoteException;

    public void addStation(Station station) throws RemoteException;

    public void setMainStation(MainStation mainStation) throws RemoteException;

    public void setMural(Mural mural) throws RemoteException;

    public void setPalette(Palette palette) throws RemoteException;

    public void setStates(States state) throws RemoteException;

    public void setStation(Station station) throws RemoteException;

    public void removeMainStation(String name) throws RemoteException;

    public void removeMural(String name) throws RemoteException;

    public void removePalette(String name) throws RemoteException;

    public void removeStates(String name) throws RemoteException;

    public void removeStation(String name) throws RemoteException;
}
