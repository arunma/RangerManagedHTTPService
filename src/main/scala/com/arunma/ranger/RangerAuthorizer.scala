package com.arunma.ranger

import org.apache.ranger.plugin.audit.RangerDefaultAuditHandler
import org.apache.ranger.plugin.policyengine.{RangerAccessRequestImpl, RangerAccessResourceImpl}
import org.apache.ranger.plugin.service.RangerBasePlugin

import scala.collection.JavaConverters._

object RangerAuthorizer {

  lazy val plugin = {
    val plg = new RangerBasePlugin("httpservice", "httpservice")
    plg.setResultProcessor(new RangerDefaultAuditHandler)
    plg.init()
    plg
  }

  def authorize(path: String, accessType: String, userName: String, userGroups: Set[String] = Set("public")): Boolean = {
    val resource = new RangerAccessResourceImpl()
    resource.setValue("path", path)
    val request = new RangerAccessRequestImpl(resource, accessType, userName, userGroups.asJava)
    val result = plugin.isAccessAllowed(request)
    result != null && result.getIsAllowed
  }
}
