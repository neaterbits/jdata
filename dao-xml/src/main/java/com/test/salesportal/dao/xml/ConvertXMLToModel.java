package com.test.salesportal.dao.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.test.salesportal.model.cv.CV;
import com.test.salesportal.model.cv.CVItem;
import com.test.salesportal.model.cv.Custom;
import com.test.salesportal.model.cv.Job;
import com.test.salesportal.model.cv.Language;
import com.test.salesportal.model.cv.Name;
import com.test.salesportal.model.cv.Personalia;
import com.test.salesportal.model.cv.Skill;
import com.test.salesportal.model.cv.Work;
import com.test.salesportal.model.text.Text;
import com.test.salesportal.model.text.Translation;
import com.test.salesportal.xml.CVType;
import com.test.salesportal.xml.CustomType;
import com.test.salesportal.xml.DescriptionType;
import com.test.salesportal.xml.ExitReasonType;
import com.test.salesportal.xml.ItemType;
import com.test.salesportal.xml.JobType;
import com.test.salesportal.xml.NameType;
import com.test.salesportal.xml.PersonaliaType;
import com.test.salesportal.xml.PositionType;
import com.test.salesportal.xml.SkillRefType;
import com.test.salesportal.xml.SkillRefsType;
import com.test.salesportal.xml.SkillType;
import com.test.salesportal.xml.SummaryType;
import com.test.salesportal.xml.TextType;
import com.test.salesportal.xml.TextsType;
import com.test.salesportal.xml.WorkType;

final class ConvertXMLToModel {
	// Convert to JPA model and use that for returning to user
	static CV convertToModel(CVType xmlCV, Language [] languages) {
		
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
			final List<CVItem> items = new ArrayList<>();
	
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

	private static List<Translation> convertTexts(TextsType xmlTexts, Language [] languages) {
		
		final List<Translation> ret;
		
		if (xmlTexts.getText() != null && !xmlTexts.getText().isEmpty()) {
			ret = new ArrayList<>(xmlTexts.getText().size());

			
			if (languages != null) {
				// Sort according to languages
				if (languages.length == 0) {
					throw new IllegalArgumentException("no languages");
				}

				// Sort by languages, so just nest loops
				boolean found = false;
				for (Language language : languages) {
					for (TextType xmlText : xmlTexts.getText()) {
						if (language.getCode().equals(xmlText.getLanguage())) {
							
							final Translation text = convertText(xmlText, language);
							
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
				// No languages, just return all texts
				for (TextType xmlText : xmlTexts.getText()) {
					
					final Language language = xmlText.getLanguage() != null
							? Language.fromCode(xmlText.getLanguage())
							: null;
					
					final Translation text = convertText(xmlText, language);
					
					ret.add(text);
				}
			}
		}
		else {
			ret = Collections.emptyList();
		}

		return ret;
	}
	
	private static Translation convertText(TextType xmlText, Language language) {
		final Translation text = new Translation();
		
		text.setLanguage(language);
		text.setText(xmlText.getText());

		return text;
	}

	private static Name convertName(NameType xmlName, Language [] languages) {
		return new Name(convertTexts(xmlName, languages));
	}

	private static List<Translation> convertExitReason(ExitReasonType xmlExitReason, Language [] languages) {
		return convertTexts(xmlExitReason, languages);
	}

	private static List<Translation> convertPosition(PositionType xmlPosition, Language [] languages) {
		return convertTexts(xmlPosition, languages);
	}

	private static Text convertSummary(SummaryType xmlSummary, Language [] languages) {
		return new Text(convertTexts(xmlSummary, languages));
	}

	private static Text convertDescription(DescriptionType xmlDescription, Language [] languages) {
		return new Text(convertTexts(xmlDescription, languages));
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

	private static void convertItem(ItemType xmlItem, CVItem ret, Language [] languages) {
		ret.setStartTime(convertCalendar(xmlItem.getStartTime()));
		ret.setEndTime(convertCalendar(xmlItem.getEndTime()));
		
		if (xmlItem.getExitReason() != null) {
			ret.setExitReason(new Text(convertExitReason(xmlItem.getExitReason(), languages)));
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
			ret.setPosition(new Text(convertPosition(xmlJob.getPosition(), languages)));
		}
		
		return ret;
	}

}
