package com.test.cv.dao.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import com.test.cv.dao.CVStorageException;
import com.test.cv.dao.ICVDAO;
import com.test.cv.model.CV;
import com.test.cv.model.Custom;
import com.test.cv.model.Item;
import com.test.cv.model.Job;
import com.test.cv.model.Language;
import com.test.cv.model.Personalia;
import com.test.cv.model.Skill;
import com.test.cv.model.Text;
import com.test.cv.model.Work;
import com.test.cv.xml.CVType;
import com.test.cv.xml.CustomType;
import com.test.cv.xml.DescriptionType;
import com.test.cv.xml.ExitReasonType;
import com.test.cv.xml.ItemType;
import com.test.cv.xml.JobType;
import com.test.cv.xml.NameType;
import com.test.cv.xml.PersonaliaType;
import com.test.cv.xml.PositionType;
import com.test.cv.xml.SkillRefType;
import com.test.cv.xml.SkillRefsType;
import com.test.cv.xml.SkillType;
import com.test.cv.xml.SummaryType;
import com.test.cv.xml.TextType;
import com.test.cv.xml.TextsType;
import com.test.cv.xml.WorkType;
import com.test.cv.xmlstorage.api.IXMLStorage;
import com.test.cv.xmlstorage.api.StorageException;

public class XMLCVDAO implements ICVDAO {
	
	private static final JAXBContext jaxbContext;
	
	static {
		try {
			jaxbContext = JAXBContext.newInstance(CVType.class);
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to initialize JAXB context", ex);
		}
	}

	private final IXMLStorage xmlStorage;
	private final Unmarshaller unmarshaller;

	public XMLCVDAO(IXMLStorage xmlStorage) {

		if (xmlStorage == null) {
			throw new IllegalArgumentException("xmlStorage == null");
		}
		
		this.xmlStorage = xmlStorage;

		try {
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException ex) {
			throw new IllegalStateException("Failed to create JAXB context", ex);
		}
	}

	@Override
	public CV findCV(String userId, Language... languages) throws CVStorageException {

		final CV ret;
		
		try {
			final InputStream inputStream = xmlStorage.getCVXMLForUser(userId);
			
			if (inputStream == null) {
				ret = null;
			}
			else {
				try {
					final CVType xmlCV = (CVType)unmarshaller.unmarshal(inputStream);
					
					ret = convertToModel(xmlCV, languages);
				} catch (JAXBException ex) {
					throw new IllegalStateException("Failed to unmarshall", ex);
				}
				finally {
					try {
						inputStream.close();
					} catch (IOException ex) {
						throw new IllegalStateException("Failed to close input stream", ex);
					}
				}
			}
		}
		catch (StorageException ex) {
			throw new CVStorageException("Failed to retrieve CV", ex); 
		}
		
		return ret;
	}

	@Override
	public void close() throws Exception {
		// Nothing to do
	}

	// Convert to JPA model and use that for returning to user
	private static CV convertToModel(CVType xmlCV, Language [] languages) {
		
		final CV ret = new CV();
		
		if (xmlCV.getPersonalia() != null) {
			ret.setPersonalia(convertPersonalia(xmlCV.getPersonalia()));
		}

		final List<SkillType> xmlSkills;
		
		if (xmlCV.getSkills() != null && xmlCV.getSkills().getSkills() != null) {
			xmlSkills = xmlCV.getSkills().getSkills();
		}
		else {
			xmlSkills = Collections.emptyList();
		}
		
		if (xmlCV.getItems() != null) {
			final List<Item> items = new ArrayList<>();
	
			if (xmlCV.getItems().getJob() != null) {
				for (JobType xmlJob : xmlCV.getItems().getJob()) {
					items.add(convertJob(xmlJob, xmlSkills, languages));
				}
			}

			if (xmlCV.getItems().getCustom() != null) {
				for (CustomType xmlCustom : xmlCV.getItems().getCustom()) {
					items.add(convertCustom(xmlCustom, languages));
				}
			}
			
			ret.setItems(items);
		}
		
		return ret;
	}

	private static List<Text> convertTexts(TextsType xmlTexts, Language [] languages) {
		
		final List<Text> ret;
		
		if (xmlTexts.getText() != null && !xmlTexts.getText().isEmpty()) {
			ret = new ArrayList<>(xmlTexts.getText().size());

			// Sort according to languages
			if (languages.length == 0) {
				throw new IllegalArgumentException("no languages");
			}
			
			// Sort by languages, so just nest loops
			boolean found = false;
			for (Language language : languages) {
				for (TextType xmlText : xmlTexts.getText()) {
					if (language.getCode().equals(xmlText.getLanguage())) {
						
						final Text text = new Text();
						
						text.setLanguage(language);
						text.setText(xmlText.getText());
						
						ret.add(text);
						found = true;
						break;
					}
				}
				
				if (found) {
					break;
				}
			}
		}
		else {
			ret = Collections.emptyList();
		}

		return ret;
	}

	private static List<Text> convertName(NameType xmlName, Language [] languages) {
		return convertTexts(xmlName, languages);
	}

	private static List<Text> convertExitReason(ExitReasonType xmlExitReason, Language [] languages) {
		return convertTexts(xmlExitReason, languages);
	}

	private static List<Text> convertPosition(PositionType xmlPosition, Language [] languages) {
		return convertTexts(xmlPosition, languages);
	}

	private static List<Text> convertSummary(SummaryType xmlSummary, Language [] languages) {
		return convertTexts(xmlSummary, languages);
	}

	private static List<Text> convertDescription(DescriptionType xmlDescription, Language [] languages) {
		return convertTexts(xmlDescription, languages);
	}
	
	private static Custom convertCustom(CustomType xmlCustom, Language [] languages) {
		
		final Custom ret = new Custom();
		
		if (xmlCustom.getName() != null) {
			ret.setName(convertName(xmlCustom.getName(), languages));
		}
		
		return ret;
	}
	
	private static Personalia convertPersonalia(PersonaliaType xmlPersonalia) {
		final Personalia ret = new Personalia();

		ret.setFirstName(xmlPersonalia.getFirstName());
		ret.setLastName(xmlPersonalia.getLastName());
		ret.setEmailAddress(xmlPersonalia.getEmailAddress());
		ret.setDateOfBirth(convertCalendar(xmlPersonalia.getDateOfBirth()));
		
		return ret;
	}

	private static void convertItem(ItemType xmlItem, Item ret, Language [] languages) {
		ret.setStartTime(convertCalendar(xmlItem.getStartTime()));
		ret.setEndTime(convertCalendar(xmlItem.getEndTime()));
		
		if (xmlItem.getExitReason() != null) {
			ret.setExitReason(convertExitReason(xmlItem.getExitReason(), languages));
		}
	}
	
	private static Date convertCalendar(XMLGregorianCalendar xmlCalendar) {
		return xmlCalendar.toGregorianCalendar().getTime();
	}
	
	private static Skill convertSkill(SkillType xmlSkill, Language [] languages) {
		final Skill ret = new Skill();
		
		if (xmlSkill.getName() != null) {
			ret.setName(convertName(xmlSkill.getName(), languages));
		}
		
		if (xmlSkill.getDescription() != null) {
			ret.setDescription(convertDescription(xmlSkill.getDescription(), languages));
		}
		
		return ret;
	}
	
	private static List<Skill> convertSkillRefs(SkillRefsType xmlSkillRefs, List<SkillType> xmlSkills, Language [] languages) {
		
		final List<Skill> ret;
		
		if (xmlSkillRefs.getSkill() != null && !xmlSkillRefs.getSkill().isEmpty()) {
			ret = new ArrayList<>(xmlSkillRefs.getSkill().size());
			
			// For each skill ref, find corresponding skill
			for (SkillRefType xmlSkillRef : xmlSkillRefs.getSkill()) {
				
				SkillType foundXmlSkill = null;
				
				for (SkillType xmlSkill : xmlSkills) {
					if (xmlSkill.getId().equals(xmlSkillRef.getId())) {
						foundXmlSkill = xmlSkill;
						break;
					}
				}
				
				if (foundXmlSkill == null) {
					throw new IllegalStateException("no skill found with id " + xmlSkillRef.getId());
				}
				
				final Skill skill = convertSkill(foundXmlSkill, languages);
				
				ret.add(skill);
			}
		}
		else {
			ret = Collections.emptyList();
		}
		
		return ret;
	}
	
	private static void convertWork(WorkType xmlWork, Work ret, List<SkillType> xmlSkills, Language [] languages) {
		convertItem(xmlWork, ret, languages);

		if (xmlWork.getSummary() != null) {
			ret.setSummary(convertSummary(xmlWork.getSummary(), languages));
		}
		
		if (xmlWork.getDescription() != null) {
			ret.setDescription(convertDescription(xmlWork.getDescription(), languages));
		}
		
		if (xmlWork.getSkills() != null && xmlWork.getSkills().getSkill() != null && !xmlWork.getSkills().getSkill().isEmpty()) {
			ret.setSkills(convertSkillRefs(xmlWork.getSkills(), xmlSkills, languages));
		}
	}

	private static Job convertJob(JobType xmlJob, List<SkillType> xmlSkills, Language [] languages) {
		final Job ret = new Job();
		
		convertWork(xmlJob, ret, xmlSkills, languages);
		
		if (xmlJob.getEmployerName() != null) {
			ret.setEmployerName(xmlJob.getEmployerName());
		}
		
		if (xmlJob.getPosition() != null) {
			ret.setPosition(convertPosition(xmlJob.getPosition(), languages));
		}
		
		return ret;
	}
}
