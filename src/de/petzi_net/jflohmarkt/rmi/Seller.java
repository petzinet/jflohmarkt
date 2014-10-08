/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

/**
 * @author axel
 *
 */
public interface Seller extends DBObject {
	
	public long getEvent() throws RemoteException;
	
	public void setEvent(long event) throws RemoteException;
	
	public int getNumber() throws RemoteException;
	
	public void setNumber(int number) throws RemoteException;
	
	public String getName() throws RemoteException;
	
	public void setName(String name) throws RemoteException;
	
	public String getGivenName() throws RemoteException;
	
	public void setGivenName(String givenName) throws RemoteException;
	
	public String getAddressAppendix() throws RemoteException;
	
	public void setAddressAppendix(String addressAppendix) throws RemoteException;
	
	public String getStreet() throws RemoteException;
	
	public void setStreet(String street) throws RemoteException;
	
	public String getZipCode() throws RemoteException;
	
	public void setZipCode(String zipCode) throws RemoteException;
	
	public String getCity() throws RemoteException;
	
	public void setCity(String city) throws RemoteException;
	
	public String getPhone() throws RemoteException;
	
	public void setPhone(String phone) throws RemoteException;
	
	public String getEmail() throws RemoteException;
	
	public void setEmail(String email) throws RemoteException;
	
	public String getAccountHolder() throws RemoteException;
	
	public void setAccountHolder(String accountHolder) throws RemoteException;
	
	public String getBIC() throws RemoteException;
	
	public void setBIC(String bic) throws RemoteException;
	
	public String getIBAN() throws RemoteException;
	
	public void setIBAN(String iban) throws RemoteException;
	
	public Date getRegistration() throws RemoteException;
	
	public void setRegistration(Date registration) throws RemoteException;

}
