//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.23 at 08:22:54 AM BOT 
//


package com.test.cv.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WorkType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.example.org/cv}ItemType">
 *       &lt;sequence>
 *         &lt;element name="summary" type="{http://www.example.org/cv}SummaryType" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.example.org/cv}DescriptionType" minOccurs="0"/>
 *         &lt;element name="skills" type="{http://www.example.org/cv}SkillRefsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkType", propOrder = {
    "summary",
    "description",
    "skills"
})
@XmlSeeAlso({
    SelfEmployedType.class,
    JobType.class
})
public class WorkType
    extends ItemType
{

    protected SummaryType summary;
    protected DescriptionType description;
    protected SkillRefsType skills;

    /**
     * Gets the value of the summary property.
     * 
     * @return
     *     possible object is
     *     {@link SummaryType }
     *     
     */
    public SummaryType getSummary() {
        return summary;
    }

    /**
     * Sets the value of the summary property.
     * 
     * @param value
     *     allowed object is
     *     {@link SummaryType }
     *     
     */
    public void setSummary(SummaryType value) {
        this.summary = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link DescriptionType }
     *     
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptionType }
     *     
     */
    public void setDescription(DescriptionType value) {
        this.description = value;
    }

    /**
     * Gets the value of the skills property.
     * 
     * @return
     *     possible object is
     *     {@link SkillRefsType }
     *     
     */
    public SkillRefsType getSkills() {
        return skills;
    }

    /**
     * Sets the value of the skills property.
     * 
     * @param value
     *     allowed object is
     *     {@link SkillRefsType }
     *     
     */
    public void setSkills(SkillRefsType value) {
        this.skills = value;
    }

}
