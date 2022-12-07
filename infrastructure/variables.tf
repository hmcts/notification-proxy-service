variable "product" {
  type    = string
  default = "ccpay"
}

variable "component" {
  type    = string
  default = "notifications-service"
}

variable "team_name" {
  type    = string
  default = "FeesAndPay"
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}


variable "common_tags" {
  type =  map(string)
}

variable "core_product" {
  type    = string
  default = "ccpay"
}

variable "postgresql_user" {
  type    = string
  default = "notifications"
}

variable "database_name" {
  type    = string
  default = "notifications"

}

variable sku_name {
  default = "GP_Gen5_2"
}

variable "sku_capacity" {
  default = "2"
}

variable "postgresql_version" {
  default = "11"
}

variable "notifications_service_gateway_certificate_thumbprints" {
  type    = list(any)
  default = [] # TODO: remove default and provide environment-specific values
}

variable "product_name" {
  type    = string
  default = "notifications"
}

variable "additional_databases" {
  default = []
}

