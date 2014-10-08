/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class SellerImpl extends AbstractDBObject implements Seller {

	private static final long serialVersionUID = 1L;

	public SellerImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "seller";
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "id";
	}

	@Override
	public long getEvent() throws RemoteException {
		Long value = (Long) getAttribute("event");
		return value == null ? -1 : value;
	}

	@Override
	public void setEvent(long event) throws RemoteException {
		setAttribute("event", event);
	}
	
	@Override
	public int getNumber() throws RemoteException {
		Integer value = (Integer) getAttribute("number");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setNumber(int number) throws RemoteException {
		setAttribute("number", number);
	}

	@Override
	public String getName() throws RemoteException {
		return (String) getAttribute("name");
	}

	@Override
	public void setName(String name) throws RemoteException {
		setAttribute("name", name);
	}

	@Override
	public String getGivenName() throws RemoteException {
		return (String) getAttribute("givenname");
	}

	@Override
	public void setGivenName(String givenName) throws RemoteException {
		setAttribute("givenname", givenName);
	}

	@Override
	public String getAddressAppendix() throws RemoteException {
		return (String) getAttribute("addressappendix");
	}

	@Override
	public void setAddressAppendix(String addressAppendix) throws RemoteException {
		setAttribute("addressappendix", addressAppendix);
	}

	@Override
	public String getStreet() throws RemoteException {
		return (String) getAttribute("street");
	}

	@Override
	public void setStreet(String street) throws RemoteException {
		setAttribute("street", street);
	}

	@Override
	public String getZipCode() throws RemoteException {
		return (String) getAttribute("zipcode");
	}

	@Override
	public void setZipCode(String zipCode) throws RemoteException {
		setAttribute("zipcode", zipCode);
	}

	@Override
	public String getCity() throws RemoteException {
		return (String) getAttribute("city");
	}

	@Override
	public void setCity(String city) throws RemoteException {
		setAttribute("city", city);
	}

	@Override
	public String getPhone() throws RemoteException {
		return (String) getAttribute("phone");
	}

	@Override
	public void setPhone(String phone) throws RemoteException {
		setAttribute("phone", phone);
	}

	@Override
	public String getEmail() throws RemoteException {
		return (String) getAttribute("email");
	}

	@Override
	public void setEmail(String email) throws RemoteException {
		setAttribute("email", email);
	}

	@Override
	public String getAccountHolder() throws RemoteException {
		return (String) getAttribute("accountholder");
	}

	@Override
	public void setAccountHolder(String accountHolder) throws RemoteException {
		setAttribute("accountholder", accountHolder);
	}

	@Override
	public String getBIC() throws RemoteException {
		return (String) getAttribute("bic");
	}

	@Override
	public void setBIC(String bic) throws RemoteException {
		setAttribute("bic", bic);
	}

	@Override
	public String getIBAN() throws RemoteException {
		return (String) getAttribute("iban");
	}

	@Override
	public void setIBAN(String iban) throws RemoteException {
		setAttribute("iban", iban);
	}

	@Override
	public Date getRegistration() throws RemoteException {
		return (Date) getAttribute("registration");
	}

	@Override
	public void setRegistration(Date registration) throws RemoteException {
		setAttribute("registration", registration);
	}

}
