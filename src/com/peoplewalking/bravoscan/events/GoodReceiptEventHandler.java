package com.peoplewalking.bravoscan.events;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

public class GoodReceiptEventHandler extends EntityPersistenceEventObserver {
	private static final Entity GOODRECEIPT_ENTITY = ModelProvider.getInstance().getEntity(ShipmentInOut.ENTITY_NAME);
	private static Entity[] entities = { GOODRECEIPT_ENTITY };
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	protected Entity[] getObservedEntities() {
		return entities;
	}

	/**
		Test comment: Write method javadoc
	**/

	public void onSave(@Observes EntityNewEvent eventNew) {
		// Validamos el evento
		if (!isValidEvent(eventNew)) {
			logger.info("Invalid event to save ShipmentInOut");
			return;
		}
		try {					
			ShipmentInOut targetShipmentInOut = (ShipmentInOut) eventNew.getTargetInstance();

			Sequence sheetSequence = targetShipmentInOut.getDocumentType().getDocumentSequence();

			// Construimos el próximo documentNo según la secuencia
			StringBuilder nextSequence = new StringBuilder();
			if(sheetSequence.getPrefix() != null) {
				nextSequence.append(sheetSequence.getPrefix());
			}
			nextSequence.append(sheetSequence.getNextAssignedNumber());
			if(sheetSequence.getSuffix() != null) {
				nextSequence.append(sheetSequence.getSuffix());
			}

			// Asignamos el valor al documentNo
			final Property DocumentNoProp = GOODRECEIPT_ENTITY.getProperty(ShipmentInOut.PROPERTY_DOCUMENTNO);
			eventNew.setCurrentState( DocumentNoProp, nextSequence.toString());

			// Actualizamos la secuencia
			Long nextAssignedNumber = sheetSequence.getNextAssignedNumber()
					+ sheetSequence.getIncrementBy();
			sheetSequence.setNextAssignedNumber(nextAssignedNumber);
		} catch (Exception e) {
			logger.error("An error has occurred while updating ShipmentInOut documentNo", e);
		}
	}

}
